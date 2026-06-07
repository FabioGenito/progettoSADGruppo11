/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
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
    private final PlaylistDAO dao; // Receiver

    public AddTrackCommand(int trackId, int playlistId, PlaylistDAO dao) {
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
            throw e; // Rilanciamo l'eccezione affinché il Client (Controller) possa intercettarla e mostrare un alert
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        try {
            // L'operazione inversa dell'aggiunta è la rimozione
            dao.removeTrackFromPlaylist(playlistId, trackId);
            System.out.println("Command Undo: Traccia " + trackId + " rimossa dalla playlist " + playlistId);
        } catch (Exception e) {
            System.err.println("Errore durante il ripristino dello stato.");
            e.printStackTrace();
        }
    }
}
