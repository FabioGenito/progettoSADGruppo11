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
import com.progettosad.mediaplayergruppo11.command.UndoManager;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

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
    @FXML private HBox undoNotificationBox;
    @FXML private Label undoMessageLabel;
    @FXML private Button undoButton;
    
    
    // Oggetto che gestisce il timer senza bloccare l'interfaccia
    private PauseTransition hideTransition;

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
    public void showUndoNotification() {
        if (undoNotificationBox != null && undoMessageLabel != null && hideTransition != null) {
            undoMessageLabel.setText("Traccia aggiunta con successo!");
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