package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.service.RecommendationEngine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
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

    private javafx.scene.Node libraryCenterNode;
    @FXML private BorderPane mainBorderPane;
    @FXML private javafx.scene.control.Button addTrackButton;
    @FXML private SidebarController sidebarController;
    @FXML private TrackTableController trackTableController;
    @FXML private PlayerBarController playerBarController;
    @FXML private HBox consigliatiContainer;

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
            loadRecommendationsAsync();
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