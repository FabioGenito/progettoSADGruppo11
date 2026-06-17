/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.command.*;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAOInterface;
import com.progettosad.mediaplayergruppo11.dao.factory.DatabaseDAOFactory;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.model.*;
import com.progettosad.mediaplayergruppo11.observer.*;
import com.progettosad.mediaplayergruppo11.view.dialogs.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Fabio
 */

public class TrackTableController implements Initializable, Observer {

    @FXML private TableView<Track> trackTableView;
    @FXML private TableColumn<Track, Void> colIndex;
    @FXML private TableColumn<Track, String> colTitolo;
    @FXML private TableColumn<Track, String> colArtista;
    @FXML private TableColumn<Track, String> colAlbum;
    @FXML private TableColumn<Track, String> colDurata;
    @FXML private TableColumn<Track, String> colGenere;
    @FXML private TableColumn<Track, Integer> colAnno;

    private Playlist currentOpenPlaylist = null; // Memorizza la playlist aperta, se c'è
    private TrackManager subject;
    
    private MainShellController mainShell;

    public void setMainShell(MainShellController mainShell) {
        this.mainShell = mainShell;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup delle colonne
        colTitolo.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        colDurata.setCellValueFactory(new PropertyValueFactory<>("formattedLength"));
        colGenere.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colAnno.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        colDurata.setStyle("-fx-alignment: CENTER_RIGHT");
        trackTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Colonna Indice personalizzata
        colIndex.setCellFactory(column -> new TableCell<>() {
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

        trackTableView.setPlaceholder(new Label("Nessun brano presente"));

        setupContextMenu();
        setupTableRows();

        // Si iscrive come Observer per le modifiche ai brani
        this.subject = TrackManager.getInstance();
        this.subject.attach(this);
        PlaybackEngine.getInstance().attach(this);

        // Caricamento iniziale di tutti i brani
        loadLibraryAsync();
    }
    
    /**
     * Metodo catturato dal bottone "+" dell'interfaccia FXML.
     * Apre il form in modalità "Inserimento" (passando null).
     */
    @FXML
    public void handleAddTrack() {
        openTrackForm(null); 
    }

    // --- METODI PUBBLICI PER IL MAIN SHELL ---
    
    public void playFirstTrack() {
        if (trackTableView != null && !trackTableView.getItems().isEmpty()) {
            Track firstTrack = trackTableView.getItems().get(0);
            PlaybackEngine.getInstance().playSelection(firstTrack, trackTableView.getItems());
            trackTableView.getSelectionModel().select(0);
            trackTableView.scrollTo(0);
        } else {
            AlertUtils.show(Alert.AlertType.WARNING, "Libreria Vuota", "Non ci sono brani da riprodurre.");
        }
    }
    /**
     * Chiamato dal MainShellController quando l'utente seleziona "Tutti i brani" o "Home"
     */
    public void loadAllTracks() {
        this.currentOpenPlaylist = null;
        loadLibraryAsync();
    }

    /**
     * Chiamato dal MainShellController quando l'utente clicca una playlist
     */
    public void loadPlaylistTracks(Playlist playlist) {
        this.currentOpenPlaylist = playlist;
        
        if (playlist.getId() == -1) {
            // CASO A: È una playlist virtuale (Mix personalizzato)
            trackTableView.setItems(FXCollections.observableArrayList(playlist.getTracks()));
            
            // Specifica T-17/03: Aggiorniamo all'istante la coda di riproduzione attiva
            if (!playlist.getTracks().isEmpty()) {
                PlaybackEngine.getInstance().playSelection(playlist.getTracks().get(0), trackTableView.getItems());
            }
            
        } else {
            // CASO B: È una playlist normale (salvata su DB)
            loadTracksByPlaylistAsync(playlist.getId());
        }
    }

    // --- CARICAMENTI ASINCRONI ---

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
    
    // --- GESTIONE DRAG & DROP E DOPPIO CLICK ---

    /**
     * TASK T-13/03: Configurazione Doppio Clic sulla TableView per la riproduzione.
     * TASK T-19/02: Implementazione del Drag & Drop o dello Spostamento Visivo
     */
    private void setupTableRows() {
        trackTableView.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setCursor(javafx.scene.Cursor.HAND);
                }
            });
            row.setOnMouseExited(event -> {
                row.setCursor(javafx.scene.Cursor.DEFAULT);
            });

            // --- 1. GESTIONE DOPPIO CLICK ---
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Track selectedTrack = row.getItem();
                    PlaybackEngine.getInstance().playSelection(selectedTrack, trackTableView.getItems());
                }
            });

            // --- 2. GESTIONE DRAG & DROP (Inizio trascinamento) ---
            row.setOnDragDetected(event -> {
                if (!row.isEmpty() && currentOpenPlaylist != null) {
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null)); 
                    
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(row.getIndex())); 
                    db.setContent(cc);
                    
                    event.consume();
                }
            });

            // --- 3. GESTIONE DRAG OVER (Passaggio sopra una riga) ---
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && currentOpenPlaylist != null) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    if (row.getIndex() != draggedIndex) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            // --- 4. GESTIONE FEEDBACK VISIVO (Mouse Entra/Esce) ---
            row.setOnDragEntered(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && currentOpenPlaylist != null) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    if (row.getIndex() != draggedIndex && !row.isEmpty()) {
                        // Applica la classe CSS
                        if (!row.getStyleClass().contains("drop-target")) {
                            row.getStyleClass().add("drop-target");
                        }
                    }
                }
                event.consume();
            });

            row.setOnDragExited(event -> {
                // Rimuove la classe CSS quando il mouse si sposta
                row.getStyleClass().remove("drop-target");
                event.consume();
            });

            // --- 5. GESTIONE RILASCIO (Drop confermato) ---
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                
                row.getStyleClass().remove("drop-target"); 
                
                if (db.hasString() && currentOpenPlaylist != null) {
                    int oldIndex = Integer.parseInt(db.getString());
                    int newIndex = row.isEmpty() ? trackTableView.getItems().size() - 1 : row.getIndex();
                    
                    // A. Creazione del comando passando gli indici e la lista della UI
                    MoveTrackCommand moveCommand = new MoveTrackCommand(
                            oldIndex, 
                            newIndex, 
                            currentOpenPlaylist.getId(), 
                            trackTableView.getItems()
                    );
                    // B. Esecuzione del comando e salvataggio nello storico 
                    moveCommand.execute();
                    UndoManager.getInstance().saveCommand(moveCommand);
                    // C. Selezione della nuova riga e Notifica a schermo
                    Platform.runLater(() -> {
                        trackTableView.getSelectionModel().select(newIndex);
                        trackTableView.refresh();
                    });
            
                    success = true;
                }
                
                event.setDropCompleted(success);
                event.consume();
            });

            return row;
        });
    }

    // --- MENU CONTESTUALE ---

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        Menu addToPlaylistMenu = new Menu("Aggiungi a playlist...");
        addToPlaylistMenu.getItems().add(new MenuItem("Caricamento in corso..."));

        addToPlaylistMenu.setOnShowing(e -> {
            addToPlaylistMenu.getItems().clear();
            try {
                List<Playlist> playlists = new PlaylistDAO().getAllPlaylists();
                if (playlists.isEmpty()) {
                    MenuItem emptyItem = new MenuItem("Nessuna playlist disponibile");
                    emptyItem.setDisable(true);
                    addToPlaylistMenu.getItems().add(emptyItem);
                } else {
                    for (Playlist p : playlists) {
                        MenuItem item = new MenuItem(p.getName());
                        item.setOnAction(event -> handleAddTrackToPlaylist(p.getId()));
                        addToPlaylistMenu.getItems().add(item);
                    }
                }
            } catch (Exception ex) {
                MenuItem errorItem = new MenuItem("Errore caricamento");
                errorItem.setDisable(true);
                addToPlaylistMenu.getItems().add(errorItem);
            }
        });

        MenuItem editItem = new MenuItem("Modifica Traccia");
        editItem.setOnAction(event -> openTrackForm(trackTableView.getSelectionModel().getSelectedItem()));

        MenuItem deleteItem = new MenuItem("Elimina Traccia");
        deleteItem.setOnAction(event -> {
            Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
            if (selectedTrack != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Conferma Eliminazione");
                confirm.setHeaderText(null);
                confirm.setContentText("Vuoi davvero eliminare \"" + selectedTrack.getTitle() + "\"?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) subject.deleteTrack(selectedTrack.getId());
                });
            }
        });

        MenuItem removeFromPlaylistItem = new MenuItem("Rimuovi da questa playlist");
        removeFromPlaylistItem.setOnAction(event -> handleRemoveTrackFromPlaylist());

        contextMenu.getItems().addAll(addToPlaylistMenu, editItem, deleteItem, removeFromPlaylistItem);

        contextMenu.setOnShowing(event -> {
            boolean siamoInLibreria = (currentOpenPlaylist == null);
            addToPlaylistMenu.setVisible(siamoInLibreria);
            editItem.setVisible(siamoInLibreria);
            deleteItem.setVisible(siamoInLibreria);
            removeFromPlaylistItem.setVisible(!siamoInLibreria);
        });

        trackTableView.setContextMenu(contextMenu);
    }

/**
     * T-09/02 e T-14/01
     * Gestisce l'aggiunta di una traccia ad una specifica playlist implementando il Command Pattern
     * per rendere l'operazione reversibile tramite UndoManager.
     */
    @FXML
    private void handleAddTrackToPlaylist(int playlistId) {
        Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
        if (selectedTrack == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                PlaylistDAOInterface playlistDAO = DatabaseDAOFactory.getInstance().getPlaylistDAO();
                // 1. Creazione del command
                Command addTrackCmd = new AddTrackCommand(selectedTrack.getId(), playlistId, playlistDAO);

                // 2. Esecuzione immediata
                addTrackCmd.execute(); 
                
                // 3. Salvataggio nella pila se l'esecuzione va a buon fine
                UndoManager.getInstance().saveCommand(addTrackCmd); 

                // 4. Aggiornamento UI e notifica Observer sul thread principale
                Platform.runLater(() -> {
                    // RICHIAMA IL BANNER FLUTTUANTE
                    if (mainShell != null) {
                        mainShell.showUndoNotification("Traccia aggiunta con successo!");
                    }
                    
                    /* Nota Architetturale: Poiché abbiamo scavalcato il PlaylistManager, 
                       forziamo qui la notifica Observer per aggiornare l'interfaccia */
                    if (subject instanceof TrackManager) {
                        subject.notifyObservers(new AppEvent(AppEventType.TRACK_ADDED_TO_PLAYLIST, playlistId));             
                    }
                });

            } catch (TrackAlreadyInPlaylistException ex) {
                // Intercetta l'eccezione lanciata dal metodo execute() del comando
                Platform.runLater(() -> {
                    AlertUtils.show(Alert.AlertType.ERROR, "Errore di inserimento", "Il brano è già presente in questa playlist.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    AlertUtils.show(Alert.AlertType.ERROR, "Errore di sistema", "Impossibile aggiungere il brano: " + ex.getMessage());
                });
            }
        });
    }
    
    /**
     * T-10/02 e T-10/03
     * Gestisce la rimozione asincrona del brano dalla playlist corrente
     * T-15/01
     * consente di revocare l'azione di rimozione di un brano dalla playlist.
     */
private void handleRemoveTrackFromPlaylist() {

    Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();

    if (selectedTrack == null || currentOpenPlaylist == null) {
        return;
    }

    // T-15/01: Salvataggio della posizione originale per consentire il corretto Undo
    int originalIndex =
            trackTableView.getSelectionModel().getSelectedIndex();

    CompletableFuture.runAsync(() -> {

        try {

            PlaylistDAOInterface dao = DatabaseDAOFactory.getInstance().getPlaylistDAO();

            // T-15/01: Creazione del Command che incapsula l'operazione
            RemoveTrackCommand cmd =
                    new RemoveTrackCommand(
                            selectedTrack.getId(),
                            currentOpenPlaylist.getId(),
                            originalIndex,
                            dao
                    );

            // T-15/01: Salvataggio nella history dell'UndoManager
            UndoManager.getInstance().saveCommand(cmd);

            // T-15/01: Esecuzione della rimozione tramite Command Pattern
            cmd.execute();

            Platform.runLater(() -> {

                // T-10/03: Rimozione istantanea visiva dalla ObservableList
                trackTableView.getItems().remove(selectedTrack);

                // T-15/02: Notifica parametrizzata con possibilità di Undo
                if (mainShell != null) {
                    mainShell.showUndoNotification("Traccia rimossa dalla playlist");
                }
             else {
                if (mainShell != null) {
                    mainShell.showUndoNotification("Errore di salvataggio");
                }}
               

            });

            // T-10/03: Lancio evento tramite l'Observer Pattern del progetto
            if (subject instanceof TrackManager) {
                subject.notifyObservers(new AppEvent(AppEventType.TRACK_REMOVED_FROM_PLAYLIST, currentOpenPlaylist.getId()));
            }

        } catch (Exception ex) {

            Platform.runLater(() -> {

                AlertUtils.show(
                        Alert.AlertType.ERROR,
                        "Errore",
                        "Impossibile rimuovere il brano: "
                                + ex.getMessage()
                );

            });
        }
    });
}

    /***
     * Metodo centralizzato per aprire il form di traccia.
     * @param trackToEdit se null apre in modalità Inserimento, se valorizzato in modalità Modifica.
     */
    private void openTrackForm(Track trackToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/FormView.fxml"));
            Parent root = loader.load();

            if (trackToEdit != null) {
                FormController controller = loader.getController();
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
    

    // --- OBSERVER ---
    @Override
    public void update(AppEvent event) {
        if (event == null) return;
        
        Platform.runLater(() -> {
            switch (event.getType()) {
                case TRACK_DELETED_FROM_DB:
                    int deletedId = (Integer) event.getPayload();
                    trackTableView.getItems().removeIf(t -> t.getId() == deletedId);
                    break;
                    
                case TRACK_ADDED_TO_DB:
                    if (currentOpenPlaylist == null) { 
                        // We are in the main library view
                        Track nuovaTraccia = (Track) event.getPayload();
                        if (nuovaTraccia != null) {
                            trackTableView.getItems().add(nuovaTraccia);
                            trackTableView.sort();
                        }
                    }
                    break;
                    
                case TRACK_UPDATED:
                    trackTableView.refresh();
                    trackTableView.sort();
                    break;
                    
                case TRACK_ADDED_TO_PLAYLIST:
                case TRACK_REMOVED_FROM_PLAYLIST:
                    int targetPlaylistId = (Integer) event.getPayload();
                    if (currentOpenPlaylist != null && currentOpenPlaylist.getId() == targetPlaylistId) {
                        loadPlaylistTracks(currentOpenPlaylist); 
                    }
                    break;
                    
                case PLAYBACK_STATE_CHANGED:
                    Track playingTrack = (Track) event.getPayload();
                    if (playingTrack != null && trackTableView != null) {
                        // Cerchiamo la traccia per ID per evitare problemi di reference in memoria
                        for (Track t : trackTableView.getItems()) {
                            if (t.getId() == playingTrack.getId()) {
                                trackTableView.getSelectionModel().select(t);
                                trackTableView.scrollTo(t);
                                break; // Trovata e selezionata, usciamo dal ciclo
                            }
                        }
                    } else if (playingTrack == null && trackTableView != null) {
                        // Opzionale: se la riproduzione si ferma del tutto, deseleziona la tabella
                        trackTableView.getSelectionModel().clearSelection();
                    }
                break;
                    
                default:
                    break;
            }
        });
    }

}
