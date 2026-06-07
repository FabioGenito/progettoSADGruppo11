package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import com.progettosad.mediaplayergruppo11.utils.DialogFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;

/**
 * FXML Controller class
 *
 * @author Fabio
 */
public class SidebarController implements Initializable, Observer {

    @FXML
    private ListView<Playlist> playlistListView;

    private MainShellController mainShell;

    public void setMainShell(MainShellController mainShell) {
        this.mainShell = mainShell;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlaylistManager.getInstance().attach(this);
        loadPlaylistsAsync();
        
        playlistListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && mainShell != null) {
                mainShell.onPlaylistSelected(newValue);
            }
        });
    }
    
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

        task.setOnFailed(event -> AlertUtils.show(Alert.AlertType.ERROR, "Errore Playlist", "Impossibile caricare le playlist dalla base dati."));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleCreatePlaylist() {
        DialogFactory.showNewPlaylistDialog().ifPresent(result -> {
            String name = result.getKey();
            String image = result.getValue();

            Task<Playlist> task = new Task<>() {
                @Override
                protected Playlist call() {
                    return PlaylistManager.getInstance().createPlaylist(name, image);
                }
            };

            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    AlertUtils.show(Alert.AlertType.INFORMATION, "Successo", "Playlist '" + name + "' creata con successo!");
                });
            });

            task.setOnFailed(event -> {
                Platform.runLater(() -> {
                    AlertUtils.show(Alert.AlertType.ERROR, "Errore", "Errore durante la creazione della Playlist.");
                });
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        });
    }
    
    @Override
    public void update() {
        String stato = PlaylistManager.getInstance().getState();
        
        if (PlaylistManager.EVENT_PLAYLIST_CREATED.equals(stato)) {
            Playlist nuovaPlaylist = PlaylistManager.getInstance().getLastCreatedPlaylist();
            
            Platform.runLater(() -> {
                if (playlistListView != null && nuovaPlaylist != null) {
                    playlistListView.getItems().add(nuovaPlaylist);
                }
            });
        }
    }
}