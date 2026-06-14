/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Utente
 */
public class MoveTrackCommand implements Command{
    private final int oldIndex;
    private final int newIndex;
    private final int playlistId;
    private final List<Track> currentTracks;

    public MoveTrackCommand(int oldIndex, int newIndex, int playlistId, List<Track> currentTracks) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.playlistId = playlistId;
        this.currentTracks = new ArrayList<>(currentTracks);
    }
    
    

    @Override
    public void execute() {
        moveElements(oldIndex, newIndex);            
        PlaybackEngine.getInstance().moveTrackInQueue(oldIndex, newIndex);
        PlaylistManager.getInstance().updatePlaylistTrackOrderAsync(playlistId, currentTracks);
    }

    @Override
    public void undo() {
        // L'operazione "inversa": riporta da newIndex a oldIndex
        moveElements(newIndex, oldIndex);
        PlaybackEngine.getInstance().moveTrackInQueue(newIndex, oldIndex);
        PlaylistManager.getInstance().updatePlaylistTrackOrderAsync(playlistId, currentTracks);
    }
    
    private void moveElements(int from, int to) {
        if (from >= 0 && from < currentTracks.size() && to >= 0 && to < currentTracks.size()) {
            Track track = currentTracks.remove(from);
            currentTracks.add(to, track);
        }
    }

}
