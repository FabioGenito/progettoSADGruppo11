/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

import com.progettosad.mediaplayergruppo11.dao.PlaylistDAOInterface;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.observer.*;
/**
 *
 * @author gcucc
 */

/**
 * Concrete Command per l'aggiunta di una traccia a una playlist.
 */
public class AddTrackCommand implements Command {
    private final int trackId;
    private final int playlistId;
    private final PlaylistDAOInterface dao;

    public AddTrackCommand(int trackId, int playlistId, PlaylistDAOInterface dao) {
        this.trackId = trackId;
        this.playlistId = playlistId;
        this.dao = dao;
    }

    @Override
    public void execute() {
        try {
            dao.addTrackToPlaylist(playlistId, trackId);
            System.out.println("Command: Traccia " + trackId + " aggiunta alla playlist " + playlistId);
        } catch (TrackAlreadyInPlaylistException e) {
            System.err.println("Impossibile eseguire: Traccia già presente.");
            throw e; 
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta della traccia.");
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        try {
            // L'operazione inversa dell'aggiunta è la rimozione
            dao.removeTrackFromPlaylist(playlistId, trackId);
            System.out.println("Command Undo: Traccia " + trackId + " rimossa dalla playlist " + playlistId);
            
            // L'OBSERVER: Avvisa l'interfaccia grafica che il brano non c'è più
            TrackManager.getInstance().notifyObservers(new AppEvent(AppEventType.TRACK_REMOVED_FROM_PLAYLIST, playlistId));                  
        } catch (Exception e) {
            System.err.println("Errore durante il ripristino dello stato.");
            e.printStackTrace();
        }
    }
}
