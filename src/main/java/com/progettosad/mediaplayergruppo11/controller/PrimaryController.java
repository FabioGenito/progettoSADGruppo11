package com.progettosad.mediaplayergruppo11.controller;


/*funge da observer per gestire le iterazione dell'utente con la libreria musicale*/
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController implements Observer {
    
    private TrackManager subject;
    
    @FXML
    private TableView<Track> tracksTable;
    @FXML
    private TableColumn<Track, String> titleColumn;
    @FXML
    private TableColumn<Track, String> artistColumn;

    public PrimaryController() {
        this.subject = new TrackManager(); 
        this.subject.attach(this); 
    }

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

        // Brani finti per la Sandbox visiva
        Track fakeTrack1 = new Track("Bohemian Rhapsody", "Queen", 10, "A Night at the Opera", 1975, "Rock", "");
        fakeTrack1.setId(1); 
        
        Track fakeTrack2 = new Track("Stairway to Heaven", "Led Zeppelin", 15, "Led Zeppelin IV", 1971, "Rock", "");
        fakeTrack2.setId(2); 

        tracksTable.getItems().addAll(fakeTrack1, fakeTrack2);
        setupContextMenu();
    }

    // AZIONI DI CONTROLLO DEL PLAYER 

    @FXML
    private void handlePlay() {
        System.out.println("Controller: Pressione tasto PLAY");
        Track tracciaSelezionata = tracksTable.getSelectionModel().getSelectedItem();
        
        // Se non hai selezionato nulla, fa partire la prima canzone della tabella per default
        if (tracciaSelezionata == null && !tracksTable.getItems().isEmpty()) {
            tracciaSelezionata = tracksTable.getItems().get(0);
        }
        
        if (tracciaSelezionata != null) {
            PlaybackEngine.getInstance().playTrack(tracciaSelezionata);
        }
    }

    @FXML
    private void handlePause() {
        System.out.println("Controller: Pressione tasto PAUSA");
        PlaybackEngine.getInstance().pauseTrack();
    }

    @FXML
    private void handleStop() {
        System.out.println("Controller: Pressione tasto STOP");
        PlaybackEngine.getInstance().stopTrack();
    }

    // Gestione Menu a tendina e finestre di dialogo
 
    private void setupContextMenu() {
        if(tracksTable == null) return;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Elimina Traccia");

        deleteItem.setOnAction(event -> {
            Track selectedTrack = tracksTable.getSelectionModel().getSelectedItem();
            if (selectedTrack != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Conferma Eliminazione");
                confirm.setHeaderText(null);
                confirm.setContentText("Vuoi davvero eliminare \"" + selectedTrack.getTitle() + "\"?");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        //Delega l'operazione al database tramite il DAO 
                        boolean esitoDb = tracksTable.getItems().remove(selectedTrack);
                       
                        if (esitoDb) {
                            subject.deleteTrack(selectedTrack.getId());
                            // Se il DB restituisce false perché la traccia è finta, aggiorniamo comunque la tabella per testare la UI
                            forceLocalUIRemoval(selectedTrack.getId());
                        }
                    }
                });
            }
        });

        contextMenu.getItems().add(deleteItem);
        tracksTable.setContextMenu(contextMenu);
    }

    private void forceLocalUIRemoval(int trackId) {
        Platform.runLater(() -> {
            tracksTable.getItems().removeIf(track -> track.getId() == trackId);
        });
    }

    // Ricezione dell'evento reattivo dal Pattern Observer per notificare un cambiamento
    @Override
    public void update() {
        String stato = subject.getState();
        if (stato != null && stato.startsWith("DELETED_TRACK_")) {
            int deletedId = Integer.parseInt(stato.split("_")[2]);
            
            Platform.runLater(() -> {
                if (tracksTable != null) {
                    tracksTable.getItems().removeIf(track -> track.getId() == deletedId);
                    
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Notifica Observer");
                    info.setHeaderText(null);
                    info.setContentText("Sincronizzazione completata.");
                    info.show();
                }
            });
        }
    }
}