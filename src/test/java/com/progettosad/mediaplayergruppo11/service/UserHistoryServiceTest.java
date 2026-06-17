/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;
import com.progettosad.mediaplayergruppo11.dao.FakePlaylistDAO;
import com.progettosad.mediaplayergruppo11.dao.FakeTrackDAO;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author gcucc
 */

/**
 * Classe di test per UserHistoryService.
 * Verifica che il service gestisca correttamente i dati di ritorno dai DAO,
 * prevenendo NullPointerException e gestendo eventuali crash dei database.
 */
public class UserHistoryServiceTest {

    // TEST GET MOST PLAYED TRACKS
    @Test
    @DisplayName("getMostPlayedTracks dovrebbe ritornare la lista di brani se il DAO ha successo")
    void testGetMostPlayedTracks_Success() {
        // Creiamo un FakeTrackDAO che forza il ritorno di due tracce fittizie
        FakeTrackDAO mockTrackDAO = new FakeTrackDAO() {
            @Override
            public List<Track> getMostPlayedTracksByUser(int userId, int limit) {
                return Arrays.asList(new Track(), new Track());
            }
        };
        FakePlaylistDAO dummyPlaylistDAO = new FakePlaylistDAO();

        UserHistoryService service = new UserHistoryService(mockTrackDAO, dummyPlaylistDAO);
        List<Track> result = service.getMostPlayedTracks(1, 10);

        assertEquals(2, result.size(), "Dovrebbe ritornare esattamente i 2 brani simulati dal DAO.");
    }

    @Test
    @DisplayName("getMostPlayedTracks dovrebbe ritornare una lista vuota se il DAO ritorna null (Sicurezza NullPointer)")
    void testGetMostPlayedTracks_HandlesNull() {
        FakeTrackDAO mockTrackDAO = new FakeTrackDAO() {
            @Override
            public List<Track> getMostPlayedTracksByUser(int userId, int limit) {
                return null; // Simuliamo un malfunzionamento del DAO che ritorna null
            }
        };
        FakePlaylistDAO dummyPlaylistDAO = new FakePlaylistDAO();

        UserHistoryService service = new UserHistoryService(mockTrackDAO, dummyPlaylistDAO);
        List<Track> result = service.getMostPlayedTracks(1, 10);

        assertNotNull(result, "Il risultato non deve MAI essere null.");
        assertTrue(result.isEmpty(), "Se il DAO ritorna null, il service deve restituire una lista vuota.");
    }

    @Test
    @DisplayName("getMostPlayedTracks dovrebbe ritornare una lista vuota se il DAO lancia un'eccezione (Resilienza)")
    void testGetMostPlayedTracks_HandlesException() {
        FakeTrackDAO mockTrackDAO = new FakeTrackDAO() {
            @Override
            public List<Track> getMostPlayedTracksByUser(int userId, int limit) {
                throw new RuntimeException("Connessione al database persa!");
            }
        };
        FakePlaylistDAO dummyPlaylistDAO = new FakePlaylistDAO();

        UserHistoryService service = new UserHistoryService(mockTrackDAO, dummyPlaylistDAO);
        
        // Non usiamo assertThrows perché il Service DEVE catturare l'eccezione internamente
        List<Track> result = service.getMostPlayedTracks(1, 10);

        assertNotNull(result, "Il risultato non deve essere null anche dopo un'eccezione.");
        assertTrue(result.isEmpty(), "In caso di eccezione del DB, il service deve salvare il sistema ritornando una lista vuota.");
    }

    // TEST GET MOST PLAYED PLAYLISTS
    @Test
    @DisplayName("getMostPlayedPlaylists dovrebbe ritornare la lista di playlist se il DAO ha successo")
    void testGetMostPlayedPlaylists_Success() {
        FakeTrackDAO dummyTrackDAO = new FakeTrackDAO();
        FakePlaylistDAO mockPlaylistDAO = new FakePlaylistDAO() {
            @Override
            public List<Playlist> getMostPlayedPlaylistsByUser(int userId, int limit) {
                return Arrays.asList(new Playlist(1, "Mix", ""), new Playlist(2, "Rock", ""));
            }
        };

        UserHistoryService service = new UserHistoryService(dummyTrackDAO, mockPlaylistDAO);
        List<Playlist> result = service.getMostPlayedPlaylists(1, 10);

        assertEquals(2, result.size(), "Dovrebbe ritornare esattamente le 2 playlist simulate dal DAO.");
    }

    @Test
    @DisplayName("getMostPlayedPlaylists dovrebbe ritornare una lista vuota se il DAO ritorna null")
    void testGetMostPlayedPlaylists_HandlesNull() {
        FakeTrackDAO dummyTrackDAO = new FakeTrackDAO();
        FakePlaylistDAO mockPlaylistDAO = new FakePlaylistDAO() {
            @Override
            public List<Playlist> getMostPlayedPlaylistsByUser(int userId, int limit) {
                return null;
            }
        };

        UserHistoryService service = new UserHistoryService(dummyTrackDAO, mockPlaylistDAO);
        List<Playlist> result = service.getMostPlayedPlaylists(1, 10);

        assertNotNull(result, "Il risultato non deve MAI essere null.");
        assertTrue(result.isEmpty(), "Se il DAO ritorna null, il service deve restituire una lista vuota.");
    }

    @Test
    @DisplayName("getMostPlayedPlaylists dovrebbe ritornare una lista vuota se il DAO lancia un'eccezione")
    void testGetMostPlayedPlaylists_HandlesException() {
        FakeTrackDAO dummyTrackDAO = new FakeTrackDAO();
        FakePlaylistDAO mockPlaylistDAO = new FakePlaylistDAO() {
            @Override
            public List<Playlist> getMostPlayedPlaylistsByUser(int userId, int limit) {
                throw new RuntimeException("Errore SQL generico");
            }
        };

        UserHistoryService service = new UserHistoryService(dummyTrackDAO, mockPlaylistDAO);
        List<Playlist> result = service.getMostPlayedPlaylists(1, 10);

        assertNotNull(result, "Il risultato non deve essere null anche dopo un'eccezione.");
        assertTrue(result.isEmpty(), "In caso di eccezione del DB, il service deve salvare il sistema ritornando una lista vuota.");
    }
}