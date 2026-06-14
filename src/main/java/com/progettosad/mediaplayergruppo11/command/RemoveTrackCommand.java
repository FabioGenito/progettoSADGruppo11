/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.model.TrackManager;

/**
 *
 * @author Lara
 */
public class RemoveTrackCommand implements Command{
    
    private final int trackId;
    private final int playlistId;
    private final int originalIndex;    
    private final PlaylistDAO playlistDAO;

    public RemoveTrackCommand(int trackId, int playlistId, int originalIndex, PlaylistDAO playlistDAO) {
        this.trackId = trackId;
        this.playlistId = playlistId;
        this.originalIndex = originalIndex;
        this.playlistDAO = playlistDAO;
    }

    @Override
    public void execute() {
         try {

            playlistDAO.removeTrackFromPlaylist(
                    playlistId,
                    trackId
            );

            System.out.println(
                    "Command: Traccia "
                    + trackId
                    + " rimossa dalla playlist "
                    + playlistId
            );

        } catch (Exception e) {

            System.err.println(
                    "Errore durante la rimozione della traccia."
            );

            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
         try {
        playlistDAO.addTrackToPlaylist(
                playlistId,
                trackId,
                originalIndex
        );
        System.out.println("Command Undo: Traccia " + trackId + " reinserita nella posizione " + originalIndex);
        
        // Observer Pattern
        TrackManager.getInstance().setState("REMOVED_FROM_PLAYLIST_" + playlistId);
        } catch (Exception e) {
            System.err.println("Errore durante il ripristino.");
            e.printStackTrace();
        }
    }
}
    

