/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;
import com.progettosad.mediaplayergruppo11.dao.FakePlaylistDAO;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 *
 * @author gcucc
 */


public class RemoveTrackCommandTest {

    // TEST ESECUZIONE (EXECUTE)
    @Test
    void testExecute_RemovesTrackFromPlaylist() {
        //Setup fake database
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        Playlist playlist = fakeDao.createPlaylist("Playlist Test Execute", "img.png");
        
        //Aggiungiamo una traccia iniziale per poterla rimuovere
        int trackId = 100;
        fakeDao.addTrackToPlaylist(playlist.getId(), trackId);
        
        // Verifichiamo il setup iniziale
        assertEquals(1, fakeDao.getTracksByPlaylist(playlist.getId()).size(), "La playlist deve contenere 1 traccia prima dell'esecuzione.");

        //Creaiamo il Comando
        int originalIndex = 0; // Posizione originaria della traccia
        RemoveTrackCommand command = new RemoveTrackCommand(trackId, playlist.getId(), originalIndex, fakeDao);

        command.execute();

        //Verifica: la playlist nel DAO deve ora essere vuota
        assertTrue(fakeDao.getTracksByPlaylist(playlist.getId()).isEmpty(), "Dopo l'esecuzione del comando, la traccia deve essere stata rimossa.");
    }

    // TEST ANNULLAMENTO (UNDO)
    @Test
    void testUndo_RestoresTrackToPlaylist() {
        // Setup del finto database
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        Playlist playlist = fakeDao.createPlaylist("Playlist Test Undo", "img.png");
        int trackId = 250;
        int originalIndex = 0;
        
        //Creazione del Comando
        RemoveTrackCommand command = new RemoveTrackCommand(trackId, playlist.getId(), originalIndex, fakeDao);
        
        //Annullamento (Undo)
        command.undo();

        //Verifica: l'undo deve aver richiamato addTrackToPlaylist e reinserito il brano
        assertEquals(1, fakeDao.getTracksByPlaylist(playlist.getId()).size(), "Dopo l'undo, la traccia deve essere stata reinserita nel database.");
        assertEquals(trackId, fakeDao.getTracksByPlaylist(playlist.getId()).get(0).getId(), "La traccia reinserita deve avere l'ID corretto (250).");
    }
}
