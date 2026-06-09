/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 *
 * @author Utente
 */
public class MoveTrackCommand implements Command{
    private int oldIndex;
    private int newIndex;
    private int playlistId;
    private ObservableList<Track> uiList;

    public MoveTrackCommand(int oldIndex, int newIndex, int playlistId, ObservableList<Track> uiList) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.playlistId = playlistId;
        this.uiList = uiList;
    }
    
    

    @Override
    public void execute() {
        Platform.runLater(() -> {
            Track track = uiList.remove(oldIndex);
            uiList.add(newIndex, track);
            
            PlaybackEngine.getInstance().moveTrackInQueue(oldIndex, newIndex);
            PlaylistManager.getInstance().updatePlaylistTrackOrderAsync(playlistId, new ArrayList<>(uiList));
        });    }

    @Override
    public void undo() {
        // L'operazione "inversa": riporta da newIndex a oldIndex
        Platform.runLater(() -> {
            Track track = uiList.remove(newIndex);
            uiList.add(oldIndex, track);
            
            // Applica la logica inversa anche al backend e al database
            PlaybackEngine.getInstance().moveTrackInQueue(newIndex, oldIndex);
            PlaylistManager.getInstance().updatePlaylistTrackOrderAsync(playlistId, new ArrayList<>(uiList));
        });
    }

    
}
