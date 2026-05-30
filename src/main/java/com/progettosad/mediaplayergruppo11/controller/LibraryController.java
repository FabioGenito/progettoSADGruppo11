package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Controller della vista principale della libreria musicale.
 * Gestisce il caricamento e l'interazione con la lista delle playlist 
 * e la tabella dei brani suggeriti/in riproduzione.
 * * @author FabioGenito
 */
public class LibraryController implements Initializable, Observer{

    private TrackManager subject;
    @FXML
    private ListView<Playlist> playlistListView;

    @FXML
    private TableView<Track> trackTableView;

    @FXML
    private TableColumn<Track, Void> colIndex;
    
    @FXML
    private TableColumn<Track, String> colTitolo;

    @FXML
    private TableColumn<Track, String> colArtista;

    @FXML
    private TableColumn<Track, String> colAlbum;

    @FXML
    private TableColumn<Track, Integer> colDurata;
    
    public LibraryController() {
        this.subject = new TrackManager();
        this.subject.attach(this);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        colTitolo.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        colDurata.setCellValueFactory(new PropertyValueFactory<>("length"));

        /* * Formattazione custom per la durata:
         * Il Modello restituisce i secondi totali come intero. 
         * Utilizziamo una CellFactory per convertire dinamicamente questo valore 
         * nel classico formato stringa "MM:SS" richiesto dalla UI.
         */
        colDurata.setCellFactory(column -> new TableCell<Track, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    int minuti = item / 60;
                    int secondi = item % 60;                    
                    setText(String.format("%02d:%02d", minuti, secondi));
                }
            }
        });
        
        /* * Generazione automatica dell'indice di riga:
         * La colonna non è legata a un attributo del Modello, ma sfrutta 
         * l'indice della TableView per mostrare il numero progressivo del brano.
         */
        colIndex.setCellFactory(column -> new TableCell<Track, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        colDurata.setStyle("-fx-alignment: CENTER_RIGHT;");
        
        setupContextMenu();
    }
    
    @FXML
    private void handlePlay(){
        System.out.println("Pressione tasto Play");
        if (trackTableView.getItems().isEmpty()) {
            Alert emptyAlert = new Alert(Alert.AlertType.WARNING);
            emptyAlert.setTitle("Libreria Vuota");
            emptyAlert.setHeaderText(null);
            emptyAlert.setContentText("Non ci sono tracce disponibili. Impossibile avviare la riproduzione.");
            emptyAlert.show();
            return; 
        }

        Track tracciaSelezionata = trackTableView.getSelectionModel().getSelectedItem();
        if (tracciaSelezionata == null) {
            tracciaSelezionata = trackTableView.getItems().get(0);
            trackTableView.getSelectionModel().select(0);
        }
        PlaybackEngine.getInstance().playTrack(tracciaSelezionata);
    }

    @FXML
    private void handlePause() {
        System.out.println("Pressione tasto PAUSA");
        PlaybackEngine.getInstance().pauseTrack();
    }

    @FXML
    private void handleStop() {
        System.out.println("Pressione tasto STOP");
        PlaybackEngine.getInstance().stopTrack();
    }

    private void setupContextMenu() {
        if(trackTableView == null) return;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Elimina Traccia");

        deleteItem.setOnAction(event -> {
            Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
            if (selectedTrack != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Conferma Eliminazione");
                confirm.setHeaderText(null);
                confirm.setContentText("Vuoi davvero eliminare \"" + selectedTrack.getTitle() + "\"?");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean esitoDb = trackTableView.getItems().remove(selectedTrack);
                        if (esitoDb) {
                            subject.deleteTrack(selectedTrack.getId());
                            Platform.runLater(() -> trackTableView.getItems().removeIf(t -> t.getId() == selectedTrack.getId()));
                        }
                    }
                });
            }
        });
        contextMenu.getItems().add(deleteItem);
        trackTableView.setContextMenu(contextMenu);
    }

    @Override
    public void update() {
        String stato = subject.getState();
        if (stato != null && stato.startsWith("DELETED_TRACK_")) {
            int deletedId = Integer.parseInt(stato.split("_")[2]);
            Platform.runLater(() -> {
                if (trackTableView != null) {
                    trackTableView.getItems().removeIf(track -> track.getId() == deletedId);
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Notifica Observer");
                    info.setHeaderText(null);
                    info.setContentText("Sincronizzazione completata");
                    info.show();
                }
            });
        }
    }
}
