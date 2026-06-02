/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author Fabio
 */

class PlaylistManagerTest {

    private PlaylistManager playlistManager;
    private FakePlaylistDAO fakeDao;
    private boolean observerNotified; 

    // Costanti per i dati di test
    private static final int MOCK_PLAYLIST_ID = 1;
    private static final int MOCK_TRACK_ID = 10;

    @BeforeEach
    void setUp() {
        fakeDao = new FakePlaylistDAO();
        
        playlistManager = PlaylistManager.getInstance();
        
        playlistManager.setDao(fakeDao);

        observerNotified = false;
        Observer spyObserver = new Observer() {
            @Override
            public void update() {
                observerNotified = true;
            }
        };
        playlistManager.attach(spyObserver);
        
        fakeDao.createPlaylist("Rock Classics", "rock.png");
    }

    @Test
    @DisplayName("L'aggiunta di una nuova traccia a una playlist aggiorna lo stato e notifica gli Observer")
    void testAddTrackToPlaylistSuccess() {
        
        playlistManager.addTrackToPlaylist(MOCK_PLAYLIST_ID, MOCK_TRACK_ID);

        assertEquals(PlaylistManager.EVENT_TRACK_ADDED_TO_PLAYLIST_PREFIX + MOCK_PLAYLIST_ID, playlistManager.getState(), 
            "Lo stato deve riflettere l'ID della playlist modificata");
        assertTrue(observerNotified, "L'Observer doveva essere notificato dell'avvenuto inserimento");
    }

    @Test
    @DisplayName("Il manager intercetta silenziosamente i duplicati senza crashare o notificare la UI")
    void testAddTrackAlreadyInPlaylist() {
        playlistManager.addTrackToPlaylist(MOCK_PLAYLIST_ID, MOCK_TRACK_ID);
        
        observerNotified = false;

        assertDoesNotThrow(() -> {
            playlistManager.addTrackToPlaylist(MOCK_PLAYLIST_ID, MOCK_TRACK_ID);
        }, "Il Manager deve catturare l'eccezione TrackAlreadyInPlaylistException internamente");

        assertFalse(observerNotified, 
            "Se l'inserimento viene bloccato (duplicato), l'Observer NON deve essere notificato");
    }
}
