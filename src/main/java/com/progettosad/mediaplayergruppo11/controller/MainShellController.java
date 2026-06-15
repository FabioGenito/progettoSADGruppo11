package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.service.RecommendationEngine;

import com.progettosad.mediaplayergruppo11.view.dialogs.AlertUtils;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.dao.factory.DatabaseDAOFactory;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.service.UserHistoryService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import java.util.Collections;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Controller della vista principale della libreria musicale.
 * Gestisce il caricamento e l'interazione con la lista delle playlist 
 * e la tabella dei brani suggeriti/in riproduzione.
 * * @author Fabio e irene
 */

public class MainShellController implements Initializable {

    public static final int CURRENT_USER_ID=1;
    private Node libraryCenterNode;
    @FXML private BorderPane mainBorderPane;
    @FXML private SidebarController sidebarController;
    @FXML private TrackTableController trackTableController;
    @FXML private PlayerBarController playerBarController;
    @FXML private HBox undoNotificationBox;
    @FXML private Label undoMessageLabel;
    @FXML private Button undoButton;
    @FXML private HBox consigliatiContainer;
   
    
    // Oggetto che gestisce il timer senza bloccare l'interfaccia
    private PauseTransition hideTransition;
        private static class UserHistoryResult {
        final List<Track>    tracks;
        final List<Playlist> playlists;
 
        UserHistoryResult(List<Track> tracks, List<Playlist> playlists) {
            this.tracks    = tracks;
            this.playlists = playlists;
        }
    }

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        javafx.application.Platform.runLater(() -> {
            libraryCenterNode = mainBorderPane.getCenter();
            
            if (sidebarController != null) {
                sidebarController.setMainShell(this);
            }
            if (playerBarController != null) {
                playerBarController.setMainShell(this);
            }
            if (trackTableController != null) {
                trackTableController.setMainShell(this);
            }
            loadRecommendationsAsync();
            loadUserHistoryAsync();
        });
        
        // --- 1. CONFIGURAZIONE DEL BANNER DI NOTIFICA E UNDO (T-14/02) ---
        // Inizializzazione della transizione di 5 secondi senza bloccare l'interfaccia
        hideTransition = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        
        // Azione da compiere alla fine dei 5 secondi (nascondere il banner)
        hideTransition.setOnFinished(event -> undoNotificationBox.setVisible(false));

        // Azione associata al click del pulsante "Annulla"
        undoButton.setOnAction(event -> {
            // Ferma il timer per evitare che nasconda il banner graficamente in futuro
            hideTransition.stop();
            
            // Nasconde immediatamente il banner visivo
            undoNotificationBox.setVisible(false);
            
            // Innesca il ripristino ed estrae l'ultimo comando memorizzato nell'Invoker
            com.progettosad.mediaplayergruppo11.command.UndoManager.getInstance().undoLastCommand();
        });

    }
    
    /**
     * T-17/03: Carica i mix personalizzati in background e genera l'UI.
     */
    private void loadRecommendationsAsync() {
        Task<List<Playlist>> recommendationTask = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                return RecommendationEngine.getInstance().getCustomPlaylists(15);
            }
        };

        recommendationTask.setOnSucceeded(event -> {
            List<Playlist> customMixes = recommendationTask.getValue();
            
            for (Playlist mix : customMixes) {
                VBox card = createMixCard(mix);
                consigliatiContainer.getChildren().add(card);
            }
        });

        recommendationTask.setOnFailed(event -> {
            System.err.println("Errore nel caricamento delle raccomandazioni (T-17/03).");
            recommendationTask.getException().printStackTrace();
        });

        Thread thread = new Thread(recommendationTask);
        thread.setDaemon(true);
        thread.start();
    }
    
        /** T16/01 T-16/02
     * Esegue in background le due query DAO tramite UserHistoryService
     * e aggiorna l'interfaccia al completamento senza mai bloccare il thread UI.
     */
    private void loadUserHistoryAsync() {
 
        // Istanziamo il servizio con le implementazioni concrete dei DAO
        UserHistoryService historyService = new UserHistoryService(
            DatabaseDAOFactory.getInstance().getTrackDAO(),
            DatabaseDAOFactory.getInstance().getPlaylistDAO()
        );
 
        Task<UserHistoryResult> historyTask = new Task<>() {
            @Override
            protected UserHistoryResult call() {
                List<Track>    topTracks    = historyService.getMostPlayedTracks(CURRENT_USER_ID, 10);
                List<Playlist> topPlaylists = historyService.getMostPlayedPlaylists(CURRENT_USER_ID, 10);
                return new UserHistoryResult(topTracks, topPlaylists);
            }
        };
 
        
        historyTask.setOnSucceeded(event -> {
            UserHistoryResult result = historyTask.getValue();

            // Una sola card "cartella" per i brani più ascoltati
            if (!result.tracks.isEmpty()) {
                Playlist tracksMix = new Playlist(-1, "I tuoi brani più ascoltati", null);
                tracksMix.setTracks(result.tracks);
                consigliatiContainer.getChildren().add(createMixCard(tracksMix));
            }

            // Una card per ciascuna playlist più ascoltata
            if (!result.playlists.isEmpty()) {
                VBox card = createMixCard(new Playlist(-1, "Le tue playlist più ascoltate", null));
                card.setOnMouseClicked(e -> onPlaylistSelected(result.playlists.get(0)));
                consigliatiContainer.getChildren().add(card);
            }
            });
 
        // (eccezione imprevista)
        historyTask.setOnFailed(event -> {
            System.err.println("loadUserHistoryAsync: task fallito.");
            historyTask.getException().printStackTrace();
            });
 
        Thread t = new Thread(historyTask);
        t.setDaemon(true);
        t.start();
    }

    
    /**
     * Helper per la creazione visiva della Card e gestione eventi click (T-17/03)
     */
    private VBox createMixCard(Playlist mix) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(180, 220);
        card.setMinSize(180, 220);
        card.setStyle("-fx-background-color: #181818; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 15;");

        // Finta immagine di copertina
        Region coverImage = new Region();
        coverImage.setPrefSize(150, 150);
        coverImage.setMinSize(150, 150);
        coverImage.setStyle("-fx-background-color: #333333; -fx-background-radius: 6;");
        
        Label titleLabel = new Label(mix.getName());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        card.getChildren().addAll(coverImage, titleLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #181818; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 15;"));

        // EVENTO CLICK (T-17/03)
        card.setOnMouseClicked(event -> {
            this.onPlaylistSelected(mix);
        });

        return card;
    }
        /**
     * Crea la card visiva per un singolo BRANO.
     * Al click invia la traccia al PlaybackEngine.
     */
    private VBox createTrackCard(Track track) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(180, 220);
        card.setMinSize(180, 220);
        applyCardStyle(card, false);
 
        // Copertina (placeholder colorato, in attesa di immagini reali)
        Region cover = new Region();
        cover.setPrefSize(130, 130);
        cover.setMinSize(130, 130);
        cover.setStyle("-fx-background-color: #333333; -fx-background-radius: 6;");
 
        Label titleLabel = new Label(track.getTitle());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setMaxWidth(140);
        titleLabel.setWrapText(true);
 
        Label artistLabel = new Label(track.getArtist());
        artistLabel.setTextFill(Color.web("#a7a7a7"));
        artistLabel.setFont(Font.font("System", 11));
        artistLabel.setMaxWidth(140);
 
        card.getChildren().addAll(cover, titleLabel, artistLabel);
 
        // Hover effect
        card.setOnMouseEntered(e -> applyCardStyle(card, true));
        card.setOnMouseExited(e -> applyCardStyle(card, false));
 
        // ── Click: invia il brano al PlaybackEngine ──────────────────────────────
        card.setOnMouseClicked(e ->
            PlaybackEngine.getInstance()
                          .playSelection(track, Collections.singletonList(track))
        );
 
        return card;
    }

    /**
     * Crea la card visiva per una PLAYLIST.
     * Al click carica le tracce in background e avvia la riproduzione.
     */
    private VBox createPlaylistCard(Playlist playlist) {
       VBox card = new VBox(12);
        card.setPrefSize(180, 220);
        card.setMinSize(180, 220);
        Region coverImage= new Region();
        coverImage.setPrefSize(150, 150);
        coverImage.setMinSize(150, 150);
        coverImage.setStyle("-fx-background-color: #333333; ...");
        Label nameLabel = new Label(playlist.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLabel.setMaxWidth(140);
        nameLabel.setWrapText(true);
 
        card.getChildren().addAll(coverImage, nameLabel);
 
        card.setOnMouseEntered(e -> applyCardStyle(card, true));
        card.setOnMouseExited(e -> applyCardStyle(card, false));
 
        // ── Click: mostra nel table view E avvia la riproduzione ────────────────
        card.setOnMouseClicked(e -> {
            // Mostra la playlist nella tabella centrale
            this.onPlaylistSelected(playlist);
 
            // Carica i brani in background e li invia al PlaybackEngine
            Task<List<Track>> playTask = new Task<>() {
                @Override
                protected List<Track> call() {
                    return PlaylistManager.getInstance().getTracksByPlaylist(playlist.getId());
                }
            };
            playTask.setOnSucceeded(ev -> {
                List<Track> tracce = playTask.getValue();
                if (!tracce.isEmpty()) {
                    PlaybackEngine.getInstance().playSelection(tracce.get(0), tracce);
                }
            });
            Thread t = new Thread(playTask);
            t.setDaemon(true);
            t.start();
        });
 
        return card;
    }
    /**
     * Helper: applica lo stile scuro alla card (hover on/off).
     */
    private void applyCardStyle(VBox card, boolean hovered) {
        String bg = hovered ? "#282828" : "#181818";
        card.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 15;"
        );
    }
/**
     * Helper: crea il Label placeholder per sezioni vuote.
     */
    private Label createPlaceholderLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#a7a7a7"));
        label.setFont(Font.font("System", 13));
        label.setWrapText(true);
        label.setMaxWidth(600);
        label.setPadding(new javafx.geometry.Insets(20, 0, 10, 0));
        return label;
    }

    // --- METODI DI NAVIGAZIONE GLOBALE ---

    /**
     * Intercetta il click sul bottone fluttuante "+" e delega
     * l'apertura del form al controller della tabella.
     */
    @FXML
    private void handleAddTrack() {
        if (trackTableController != null) {
            trackTableController.handleAddTrack();
        }
    }
    
    /**
     *Rende visibile il banner di Undo, imposta il testo e fa partire il timer.
     * Viene chiamato dai sotto-controller (es. TrackTableController).
     */
    public void showUndoNotification(String message) {
        if (undoNotificationBox != null && undoMessageLabel != null && hideTransition != null) {
            undoMessageLabel.setText(message);
            undoNotificationBox.setVisible(true);
            
            // playFromStart() garantisce che se si aggiungono due tracce rapidamente, 
            // il timer di 5 secondi riparta da zero senza far sparire il banner in anticipo.
            hideTransition.playFromStart();
        }
    }
    
    /**
    * Richiamato dal PlayerBarController quando si preme Play senza brani in coda.
    */
    public void playFirstAvailableTrack() {
       mainBorderPane.setCenter(libraryCenterNode);
       if (trackTableController != null) {
           trackTableController.playFirstTrack();
       }
    }
    
    /**
     * Richiamato dal bottone Home della barra superiore
     */
    @FXML
    public void openLibraryView() {
        mainBorderPane.setCenter(libraryCenterNode);
        
        if (trackTableController != null) {
            trackTableController.loadAllTracks();
        }
        
        if (sidebarController != null) {
            sidebarController.clearSelection();
        }
    }

    /**
     * Richiamato dal bottone Esplora della barra superiore
     */
    @FXML
    public void openExploreView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/ExploreView.fxml"));
            javafx.scene.Parent exploreRoot = loader.load();
            mainBorderPane.setCenter(exploreRoot);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore", "Impossibile caricare la vista Scopri.");
        }
        if (sidebarController != null) {
            sidebarController.clearSelection();
        }
    }

    /**
     * Richiamato DALLA SIDEBAR quando l'utente clicca una playlist
     */
    public void onPlaylistSelected(Playlist selectedPlaylist) {
        mainBorderPane.setCenter(libraryCenterNode);        
        if (trackTableController != null) {
            trackTableController.loadPlaylistTracks(selectedPlaylist);
        }
    }
}
