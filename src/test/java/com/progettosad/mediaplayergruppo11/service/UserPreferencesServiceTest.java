/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.FakeTrackDAO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Fabio
 */

public class UserPreferencesServiceTest {

    private UserPreferencesService service;
    private FakeTrackDAO stubDao;

    @BeforeEach
    public void setUp() {
        stubDao = new FakeTrackDAO();
        service = new UserPreferencesService(stubDao);
    }

    @Test
    public void testGetRecommendedGenres_WithHistory() {
        stubDao.simulateEmptyHistory = false;
        List<String> genres = service.getRecommendedGenres(2);
        
        assertEquals(2, genres.size());
        assertTrue(genres.contains("Rock"));
    }

    @Test
    public void testGetRecommendedGenres_NewUserFallback() {
        stubDao.simulateEmptyHistory = true; // Utente nuovo
        List<String> genres = service.getRecommendedGenres(2);
        
        assertEquals(2, genres.size());
        assertTrue(genres.contains("Classica"), "Deve attivare la politica di ripiego per utenti senza storico");
    }
}