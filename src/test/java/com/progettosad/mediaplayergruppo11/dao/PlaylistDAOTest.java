/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gcucc
 */
public class PlaylistDAOTest {
    
    // TEST: AGGIUNTA BRANO ALLA PLAYLIST
    @Test
    void testFakeDao_AddTrackToPlaylist() {
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        
        //Creiamo una playlist fittizia in memoria
        Playlist p = fakeDao.createPlaylist("Test Add Track", "icon.png");
        
        //Eseguiamo l'aggiunta del brano con ID 500
        boolean isAdded = fakeDao.addTrackToPlaylist(p.getId(), 500);
        
        //Verifichiamo che l'operazione restituisca true
        assertTrue(isAdded, "L'inserimento del brano deve restituire true.");
        
        //Verifichiamo che il brano sia effettivamente nella lista recuperata
        List<Track> tracks = fakeDao.getTracksByPlaylist(p.getId());
        assertEquals(1, tracks.size(), "La playlist deve contenere esattamente 1 brano.");
        assertEquals(500, tracks.get(0).getId(), "L'ID del brano inserito deve essere 500.");
    }

    // TEST: RIMOZIONE BRANO DALLA PLAYLIST
    @Test
    void testFakeDao_RemoveTrackFromPlaylist() {
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        
        //Creiamo una playlist fittizia
        Playlist p = fakeDao.createPlaylist("Test Remove Track", "icon.png");
        
        //Setup iniziale: aggiungiamo due brani (500 e 501)
        fakeDao.addTrackToPlaylist(p.getId(), 500);
        fakeDao.addTrackToPlaylist(p.getId(), 501);
        
        //Eseguiamo la rimozione del primo brano (500)
        boolean isRemoved = fakeDao.removeTrackFromPlaylist(p.getId(), 500);
        
        //Verifichiamo che la rimozione confermi il successo
        assertTrue(isRemoved, "La rimozione del brano esistente deve restituire true.");
        
        //Verifichiamo lo stato finale della lista
        List<Track> remainingTracks = fakeDao.getTracksByPlaylist(p.getId());
        assertEquals(1, remainingTracks.size(), "La playlist deve contenere solo 1 brano dopo la rimozione.");
        assertEquals(501, remainingTracks.get(0).getId(), "Il brano rimanente deve essere quello con ID 501.");
    }
    
    // TEST TASK T-20/01: SALVATAGGIO NUOVO ORDINE
    void UpdateTrackPositions() {
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        Playlist p = fakeDao.createPlaylist("Test Playlist Salvataggio", "img.png");
        
        // Setup iniziale
        fakeDao.addTrackToPlaylist(p.getId(), 10);
        fakeDao.addTrackToPlaylist(p.getId(), 20);
        
        // Esecuzione: sposto la 20 prima della 10
        List<Integer> newOrder = Arrays.asList(20, 10);
        boolean isUpdated = fakeDao.updateTrackPositions(p.getId(), newOrder);
        
        // Verifica
        assertTrue(isUpdated, "Il salvataggio del nuovo ordine deve restituire true.");
    }

    // TEST TASK T-20/02: LETTURA CON ORDINAMENTO MANTENUTO
    @Test
    void GetTracksByPlaylistMaintainsOrder() {
        FakePlaylistDAO fakeDao = new FakePlaylistDAO();
        Playlist p = fakeDao.createPlaylist("Test Playlist Lettura", "img.png");
        
        //Inseriamo 3 brani
        fakeDao.addTrackToPlaylist(p.getId(), 101);
        fakeDao.addTrackToPlaylist(p.getId(), 102);
        fakeDao.addTrackToPlaylist(p.getId(), 103);
        
        // Simuliamo che l'utente abbia riordinato i brani mettendo il 103 in cima
        List<Integer> newOrder = Arrays.asList(103, 101, 102);
        fakeDao.updateTrackPositions(p.getId(), newOrder);
        
        //Eseguiamo la lettura come farebbe l'interfaccia al riavvio
        List<Track> resultTracks = fakeDao.getTracksByPlaylist(p.getId());
        
        // Verifichiamo che la query simulata restituisca esattamente l'ordine corretto
        assertEquals(3, resultTracks.size(), "La playlist deve contenere 3 brani.");
        assertEquals(103, resultTracks.get(0).getId(), "Il primo brano deve essere 103.");
        assertEquals(101, resultTracks.get(1).getId(), "Il secondo brano deve essere 101.");
        assertEquals(102, resultTracks.get(2).getId(), "Il terzo brano deve essere 102.");
    }
    
}
