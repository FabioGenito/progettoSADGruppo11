package com.progettosad.mediaplayergruppo11.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;

import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_ADDED;
import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_DELETED_PREFIX;
import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_UPDATED_PREFIX;


import java.net.URL;
import java.util.ResourceBundle;


/**
 * Controller della vista principale della libreria musicale.
 * Gestisce il caricamento e l'interazione con la lista delle playlist 
 * e la tabella dei brani suggeriti/in riproduzione.
 * * @author Fabio e irene
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
    private TableColumn<Track, String> colDurata;
    
    
    @FXML 
    private Button addTrackButton;
    
    public LibraryController() {
        this.subject = TrackManager.getInstance();
        this.subject.attach(this);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        colTitolo.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        colDurata.setCellValueFactory(new PropertyValueFactory<>("formattedLength"));
        colDurata.setStyle("-fx-alignment: CENTER_RIGHT");
        
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
        
        addTrackButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/InsertForm.fxml"));
                Parent root = loader.load();
                Scene insertScene = new Scene(root);
                Stage stage = (Stage) addTrackButton.getScene().getWindow();
                stage.setScene(insertScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.show(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la schermata di inserimento.");
            }
        });
        setupContextMenu();
    }
    
    /**
     * Inoltra la richiesta di riproduzione al motore di playback.
     * Mantiene il Controller passivo delegando il controllo dello stato (es. libreria vuota) 
     * al Modello; intercetta eventuali eccezioni di dominio per fornire un feedback visivo all'utente.
     */
    @FXML
    private void handlePlay(){
        Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
        try {
            PlaybackEngine.getInstance().playSelection(selectedTrack, trackTableView.getItems());
        } catch(IllegalStateException e) {
            AlertUtils.show(Alert.AlertType.WARNING, "Libreria Vuota", e.getMessage());
        }
    }
    
    /**
     * Delega l'interruzione temporanea della riproduzione al PlaybackEngine.
     * Il rispetto del pattern Passive View impone che il Controller non mantenga
     * traccia dello stato in esecuzione.
     */
    @FXML
    private void handlePause() {
        PlaybackEngine.getInstance().pauseTrack();
    }

    /**
     * Delega l'arresto definitivo della riproduzione al PlaybackEngine,
     * reimpostando il ciclo di vita del brano a livello di Modello.
     */
    @FXML
    private void handleStop() {
        PlaybackEngine.getInstance().stopTrack();
    }

    /**
     * Configura il menu contestuale (tasto destro) per la tabella dei brani.
     * L'azione di conferma elimina la traccia esclusivamente dal subject (TrackManager).
     * La View non viene forzata ad aggiornarsi qui, prevenendo disallineamenti: l'aggiornamento
     * visivo avverrà solo a valle, tramite la notifica dell'Observer.
     */
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
                        subject.deleteTrack(selectedTrack.getId());
                    }
                });
            }
        });
        contextMenu.getItems().add(deleteItem);
        trackTableView.setContextMenu(contextMenu);
    }

    /**
     * Implementazione del contratto Observer. 
     * Reagisce alle variazioni di stato notificate dal TrackManager, filtrando gli eventi 
     * di eliminazione. L'aggiornamento della UI è racchiuso in Platform.runLater 
     * per garantire la thread-safety con il thread grafico di JavaFX.
     */
    @Override
    public void update() {
        String stato = subject.getState();
        
        if (stato != null) {
            // Gestione Eliminazione
            if (stato.startsWith(EVENT_TRACK_DELETED_PREFIX)) {
                int deletedId = Integer.parseInt(stato.split("_")[2]);
                Platform.runLater(() -> {
                    if (trackTableView != null) {
                        trackTableView.getItems().removeIf(track -> track.getId() == deletedId);
                    }
                });
            }
            
            // T - 01/03: Gestione Inserimento in tempo reale
            if (stato.equals(EVENT_TRACK_ADDED)) {
                Track nuovaTraccia = subject.getLastAddedTrack();
                if (nuovaTraccia != null) {
                    Platform.runLater(() -> {
                        if (trackTableView != null) {
                            trackTableView.getItems().add(nuovaTraccia);
                            trackTableView.sort();
                        }
                    });
                }
            }
            
            if (stato.startsWith(EVENT_TRACK_UPDATED_PREFIX)) {
                Platform.runLater(() -> {
                    if (trackTableView != null) {
                        // Forza la tabella a ricaricare i dati visivi
                        trackTableView.refresh(); 
                    }
                });
            }
        }
    }

}
