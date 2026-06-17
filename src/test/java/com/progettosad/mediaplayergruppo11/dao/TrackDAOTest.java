/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
/**
 *
 * @author gcucc
 */

public class TrackDAOTest {

    // TEST CRUD: INSERT, UPDATE, DELETE, GET ALL
    @Test
    void testInsertTrack_ValidTrack() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        Track t = new Track("Bohemian Rhapsody", "Queen", 355, "A Night at the Opera", 1975, "Rock", "");

        Track insertedTrack = fakeDao.insertTrack(t);

        // L'ID deve essere stato autoincrementato (partendo da 1)
        assertTrue(insertedTrack.getId() > 0, "L'ID della traccia deve essere generato e > 0");
        assertEquals(1, fakeDao.getAllTracks().size(), "Il database in memoria deve contenere esattamente 1 traccia");
    }

    @Test
    void testInsertTrack_InvalidTrackThrowsException() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        // Traccia con titolo vuoto per scatenare l'eccezione
        Track invalidTrack = new Track("", "Queen", 200, "Album", 2000, "Pop", "");

        assertThrows(IllegalArgumentException.class, () -> {
            fakeDao.insertTrack(invalidTrack);
        }, "L'inserimento di una traccia senza titolo deve lanciare IllegalArgumentException");
    }

    @Test
    void testUpdateTrack_SuccessAndFailure() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        Track t = fakeDao.insertTrack(new Track("Titolo Vecchio", "Autore", 200, "Album", 2000, "Pop", ""));
        
        // Modifichiamo il titolo
        t.setTitle("Titolo Nuovo");
        boolean isUpdated = fakeDao.updateTrack(t);
        
        assertTrue(isUpdated, "L'aggiornamento di una traccia esistente deve restituire true");
        assertEquals("Titolo Nuovo", fakeDao.getAllTracks().get(0).getTitle(), "Il titolo deve essere stato aggiornato in memoria");

        // Test fallimento con ID fittizio non presente
        Track ghostTrack = new Track("Fantasma", "Nessuno", 0, "Niente", 2000, "Pop", "");
        ghostTrack.setId(999); // Impostiamo un ID inesistente
        
        boolean isGhostUpdated = fakeDao.updateTrack(ghostTrack);
        assertFalse(isGhostUpdated, "L'aggiornamento di un ID inesistente deve restituire false");
    }

    @Test
    void testDeleteTrack() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        Track t1 = fakeDao.insertTrack(new Track("Track 1", "Artist 1", 100, "Album 1", 2000, "Pop", ""));
        Track t2 = fakeDao.insertTrack(new Track("Track 2", "Artist 2", 100, "Album 2", 2000, "Pop", ""));

        // Elimino la prima traccia
        boolean isDeleted = fakeDao.deleteTrack(t1.getId());
        
        assertTrue(isDeleted, "L'eliminazione di una traccia esistente deve restituire true");
        assertEquals(1, fakeDao.getAllTracks().size(), "Deve rimanere solo 1 traccia");
        assertEquals("Track 2", fakeDao.getAllTracks().get(0).getTitle(), "La traccia rimanente deve essere Track 2");
    }

    // TEST LOGICA DI RACCOMANDAZIONE (Usando le flag del FakeDAO)
    @Test
    void testGetTopFavoriteGenres_NormalBehavior() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        List<String> topGenres = fakeDao.getTopFavoriteGenres(2);
        
        assertEquals(2, topGenres.size(), "Deve restituire esattamente il numero di generi richiesto dal limite");
        assertEquals("Rock", topGenres.get(0), "Il primo genere mockato deve essere Rock");
    }

    @Test
    void testGetTopFavoriteGenres_EmptyHistory() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        // Alteriamo il comportamento del DAO per simulare un nuovo utente senza storico ascolti
        fakeDao.simulateEmptyHistory = true; 
        
        List<String> topGenres = fakeDao.getTopFavoriteGenres(5);
        assertTrue(topGenres.isEmpty(), "Se lo storico è vuoto, la lista dei generi preferiti deve essere vuota");
    }

    @Test
    void testGetTracksByCriteria_ReturnFewerTracks() {
        FakeTrackDAO fakeDao = new FakeTrackDAO();
        // Simuliamo il caso in cui il database ha meno tracce di quelle richieste dal limite
        fakeDao.returnFewerTracks = true;
        
        int requestedLimit = 5;
        List<Track> result = fakeDao.getTracksByCriteria(FilterType.GENRE, "Pop", requestedLimit);
        
        // Visto che returnFewerTracks fa sottrarre 2 al limite, ci aspettiamo 3 tracce
        assertEquals(3, result.size(), "Il sistema deve gestire correttamente il caso in cui ci siano meno tracce del limite richiesto");
    }
}
