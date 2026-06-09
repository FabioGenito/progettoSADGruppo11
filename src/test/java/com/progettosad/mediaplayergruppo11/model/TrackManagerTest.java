/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.dao.FakeTrackDAO;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author Fabio
 */

class TrackManagerTest {

    private TrackManager trackManager;
    private FakeTrackDAO fakeDao;
    
    // Variabile per spiare se l'Observer viene notificato
    private boolean observerNotified; 

    @BeforeEach
    void setUp() {
        fakeDao = new FakeTrackDAO();
        
        trackManager = TrackManager.getInstance();
        
        trackManager.setDao(fakeDao);

        observerNotified = false;
        Observer spyObserver = new Observer() {
            @Override
            public void update() {
                observerNotified = true;
            }
        };
        trackManager.attach(spyObserver);
    }

    @Test
    @DisplayName("L'aggiornamento di una traccia deve notificare gli Observer se va a buon fine")
    void testUpdateTrackSuccess() {
        Track existingTrack = new Track("Old Title", "Old Artist", 200, "Album", 2020, "Pop", "img.png");
        fakeDao.insertTrack(existingTrack);
        
        existingTrack.setTitle("New Title");
        
        trackManager.updateTrack(existingTrack);

        assertEquals("New Title", fakeDao.getAllTracks().get(0).getTitle(), "Il DB in memoria doveva essere aggiornato");
    }

    @Test
    @DisplayName("L'aggiornamento con dati non validi deve propagare IllegalArgumentException")
    void testUpdateTrackThrowsException() {
        Track invalidTrack = new Track(); // Senza ID valido (0)
        
        assertThrows(IllegalArgumentException.class, () -> {
            trackManager.updateTrack(invalidTrack);
        });
    }

    @Test
    @DisplayName("La cancellazione di una traccia deve notificare gli Observer e aggiornare lo stato interno")
    void testDeleteTrackNotifiesObservers() {
        Track track = new Track("To Delete", "Artist", 200, "Album", 2020, "Pop", "img.png");
        fakeDao.insertTrack(track); 
        int idToDelete = track.getId();

        trackManager.deleteTrack(idToDelete);

        assertTrue(observerNotified, "L'Observer doveva essere notificato dell'avvenuta cancellazione");
        assertEquals(TrackManager.EVENT_TRACK_DELETED + idToDelete, trackManager.getState(), "Lo stato interno del Manager deve riflettere l'eliminazione");
        assertEquals(0, fakeDao.getAllTracks().size(), "Il finto database dovrebbe ora essere vuoto");
    }
}