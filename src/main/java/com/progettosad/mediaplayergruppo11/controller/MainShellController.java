package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.ResourceBundle;

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
            
        });
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