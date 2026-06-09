/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.FakeTrackDAO;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Fabio
 */

public class PlaylistGenerationServiceTest {

    private PlaylistGenerationService service;
    private FakeTrackDAO stubDao;

    @BeforeEach
    public void setUp() {
        stubDao = new FakeTrackDAO();
        service = new PlaylistGenerationService(stubDao);
    }

    @Test
    public void testGeneratePlaylist_HappyPath() {
        int requestedTracks = 10;
        Playlist result = service.generatePlaylist(FilterType.GENRE, "Rock", requestedTracks);

        assertEquals(-1, result.getId(), "L'ID della playlist temporanea deve essere -1");
        assertEquals("Mix Rock", result.getName());
        assertEquals(requestedTracks, result.getTracks().size(), "Deve restituire esattamente i brani richiesti");
    }

    @Test
    public void testGeneratePlaylist_FewerTracksAvailable() {
        int requestedTracks = 5;
        stubDao.returnFewerTracks = true; // Configuro lo stub per simulare penuria di brani

        Playlist result = service.generatePlaylist(FilterType.GENRE, "Jazz", requestedTracks);

        assertTrue(result.getTracks().size() < requestedTracks, "Deve gestire correttamente il caso in cui il DB restituisca meno brani");
        assertEquals(3, result.getTracks().size()); // 5 richiesti - 2 (logica dello stub)
    }
}