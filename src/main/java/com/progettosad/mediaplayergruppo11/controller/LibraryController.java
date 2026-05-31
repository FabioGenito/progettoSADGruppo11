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
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;


import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;

import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_ADDED;
import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_UPDATED;
import static com.progettosad.mediaplayergruppo11.model.TrackManager.EVENT_TRACK_DELETED;
import com.progettosad.mediaplayergruppo11.utils.TimeUtils;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.ProgressBar;


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
    private Button homeButton; 
    
    @FXML 
    private Button addTrackButton;
    
    // TASK T-06/03: Riferimenti FXML per la barra del Player in basso
    
    
    @FXML 
    private Label currentTrackTitle;
    
    @FXML 
    private Label currentTrackArtist;
        
    @FXML 
    private ProgressBar playerProgressBar;
    
    @FXML 
    private Button playerButton;
    
    @FXML 
    private Label currentTrackTime;
    
    @FXML 
    private Label currentTrackDuration;
    
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
        
        addTrackButton.setOnAction(event -> openTrackForm(null));
        trackTableView.setPlaceholder(new Label("Nessun brano presente"));
        
        // TASK T-13/03: Listener sulla ListView delle playlist
        setupPlaylistSelectionListener();

        // TASK T-13/03: Evento Doppio Click sulla TableView
        setupDoubleClickEvent();

        // TASK T-13/03: Pulsante Home ("Tutti i brani") per rimuovere i filtri
        if (homeButton != null) {
            homeButton.setOnAction(event -> {
                playlistListView.getSelectionModel().clearSelection(); // Deseleziona la playlist
                loadLibraryAsync(); // Ricarica tutta la libreria
            });
        }
        
        // TASK T-13/03: Popolamento iniziale di Playlist e Tracce (in background)
        setupContextMenu();
        
        // TASK T-06/03: Gestione della BottomBar
        PlaybackEngine engine = PlaybackEngine.getInstance();

        if (playerProgressBar != null) {
            playerProgressBar.progressProperty().bind(engine.progressProperty());
        }
        
        // Aggiornamento dei metadati (titolo, tempi, artista ecc...) in caso di cambio del brano
        engine.currentTrackProperty().addListener((observable, oldTrack, newTrack) -> {
            Platform.runLater(() -> {
                if (newTrack != null) {
                    if (currentTrackTitle != null) currentTrackTitle.setText(newTrack.getTitle());
                    if (currentTrackArtist != null) currentTrackArtist.setText(newTrack.getArtist());
                }
                if (currentTrackDuration != null) {
                        currentTrackDuration.setText(newTrack.getFormattedLength());
                    }
            });
        });
        
        // Aggiornamento ogni secondo della Label di tracking del brano in riproduzione
        engine.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            Platform.runLater(() -> {
                if (currentTrackTime != null) {
                    // Formattiamo il valore intero "15" nella stringa "00:15"
                    currentTrackTime.setText(TimeUtils.formatSecondsToMinutes(newTime.intValue()));
                }
            });
        });
        
        // 
        engine.isPlayingProperty().addListener((observable, oldValue, isPlaying) -> {
            Platform.runLater(() -> {
                if (playerButton != null) {
                    // Imposta il simbolo di Pausa o Play (puoi sostituirlo con setGraphic se usi un'ImageView in FXML)
                    playerButton.setText(isPlaying ? "⏸" : "▶");
                }
            });
        });
        
        loadPlaylistsAsync();
        loadLibraryAsync();
    }
    
    /**
     * TASK T-13/03: Listener per caricare le tracce filtrate al click su una Playlist.
     */
    private void setupPlaylistSelectionListener() {
        playlistListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadTracksByPlaylistAsync(newValue.getId());
            }
        });
    }

    /**
     * TASK T-13/03: Configurazione Doppio Clic sulla TableView per la riproduzione.
     */
    private void setupDoubleClickEvent() {
        trackTableView.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Track selectedTrack = row.getItem();
                    PlaybackEngine.getInstance().playSelection(selectedTrack, trackTableView.getItems());
                }
            });
            return row;
        });
    }
    
    /**
     * Metodo centralizzato per aprire il form di traccia.
     * @param trackToEdit se null apre in modalità Inserimento, se valorizzato in modalità Modifica.
     */
    private void openTrackForm(Track trackToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/InsertForm.fxml"));
            Parent root = loader.load();

            if (trackToEdit != null) {
                TrackFormController controller = loader.getController();
                controller.setTrackData(trackToEdit);
            }

            Scene scene = new Scene(root);
            Stage stage = (Stage) trackTableView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore", "Impossibile aprire il form.");
        }
    }
    
    
    /**
     * TASK T-06/03: Questo metodo gestisce l'azione sul pulsante centrale della barra.
     * Funziona come un interruttore (Toggle) tra Play e Pausa.
     */
    @FXML
    private void handlePlayPause(){
        PlaybackEngine engine = PlaybackEngine.getInstance();
        if (engine.isPlayingProperty().get()) {
            // Se sta suonando, metti in pausa
            engine.pauseTrack();
        } else {
            // Se è in pausa, e abbiamo un brano in memoria, riprendi
            if (engine.currentTrackProperty().get() != null) {
                engine.playTrack(engine.currentTrackProperty().get());
            } else {
                // Altrimenti, preleva la selezione dalla tabella e avvia
                Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
                try {
                    engine.playSelection(selectedTrack, trackTableView.getItems());
                } catch(IllegalStateException e) {
                    AlertUtils.show(Alert.AlertType.WARNING, "Libreria Vuota", e.getMessage());
                }
            }
        }
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
        
        MenuItem editItem = new MenuItem("Modifica Traccia");
        editItem.setOnAction(event -> {
            Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
            if (selectedTrack != null) {
                openTrackForm(selectedTrack); 
            }
        });

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
        
        contextMenu.getItems().addAll(editItem, deleteItem);
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
            if (stato.startsWith(EVENT_TRACK_DELETED)) {
                int deletedId = Integer.parseInt(stato.split("_")[2]);
                Platform.runLater(() -> {
                    if (trackTableView != null) {
                        trackTableView.getItems().removeIf(track -> track.getId() == deletedId);
                    }
                });
            }
            // T - 01/03: Gestione Inserimento in tempo reale
            if (stato.equals(EVENT_TRACK_ADDED)) {
                Track nuovaTraccia = subject.getLastProcessedTrack();
                if (nuovaTraccia != null) {
                    Platform.runLater(() -> {
                        if (trackTableView != null) {
                            trackTableView.getItems().add(nuovaTraccia);
                            trackTableView.sort();
                        }
                    });
                }
            }
            if (stato.startsWith(EVENT_TRACK_UPDATED)) {
                Platform.runLater(() -> {
                    if (trackTableView != null) {
                        // Forza la tabella a ricaricare i dati visivi
                        trackTableView.refresh(); 
                        trackTableView.sort(); 
                    }
                });
            }
        }
    }
    
    /**
     * T-13/01: Metodi per caricare Tracce e Playlist in background.
     * Utilizza un Task per non bloccare il thread di rendering di JavaFX.
     */
    
    /**
     * Carica tutte le playlist nella barra laterale sinistra.
     */
    private void loadPlaylistsAsync() {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                return PlaylistManager.getInstance().getAllPlaylists();
            }
        };

        task.setOnSucceeded(event -> {
            List<Playlist> playlists = task.getValue();
            if (playlists != null) {
                playlistListView.setItems(FXCollections.observableArrayList(playlists));
            }
        });

        task.setOnFailed(event -> {
            AlertUtils.show(Alert.AlertType.ERROR, "Errore Playlist", "Impossibile caricare le playlist dalla base dati.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Carica le tracce di una singola playlist.
     */
    private void loadTracksByPlaylistAsync(int playlistId) {
        Task<List<Track>> task = new Task<>() {
            @Override
            protected List<Track> call() throws Exception {
                return PlaylistManager.getInstance().getTracksByPlaylist(playlistId);
            }
        };

        task.setOnSucceeded(event -> {
            List<Track> tracks = task.getValue();
            if (tracks != null) {
                trackTableView.setItems(FXCollections.observableArrayList(tracks));
            }
        });

        task.setOnFailed(event -> {
            AlertUtils.show(Alert.AlertType.ERROR, "Errore Playlist", "Impossibile caricare le tracce della playlist.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
    
    /**
     * T-04/03: Carica inizialmente tutte le tracce dal database in modo asincrono.
     * Utilizza un Task per non bloccare il JavaFX Application Thread.
     */
    private void loadLibraryAsync() {
        Task<java.util.List<Track>> loadTask = new Task<>() {
            @Override
            protected java.util.List<Track> call() throws Exception {
                return subject.getAllTracks();
            }
        };

        loadTask.setOnSucceeded(event -> {
            java.util.List<Track> allTracks = loadTask.getValue();
            if (trackTableView != null && allTracks != null) {
                trackTableView.getItems().setAll(allTracks);
            }
        });
        
        loadTask.setOnFailed(event -> {
            AlertUtils.show(Alert.AlertType.ERROR, "Errore di Connessione", "Impossibile caricare la libreria.");
        });

        Thread t = new Thread(loadTask);
        t.setDaemon(true);
        t.start();
    } 
}
