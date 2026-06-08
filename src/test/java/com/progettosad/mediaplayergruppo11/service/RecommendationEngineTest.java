/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import static org.junit.jupiter.api.Assertions.*;
import com.progettosad.mediaplayergruppo11.dao.FakeTrackDAO;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Fabio
 */

public class RecommendationEngineTest {

    @BeforeEach
    public void setUp() {
        FakeTrackDAO stubDao = new FakeTrackDAO();
        // Utilizziamo il metodo che abbiamo aggiunto per aggirare l'accoppiamento
        RecommendationEngine.setTestInstance(stubDao); 
    }

    @Test
    public void testGetCustomPlaylists_CorrectAssembly() {
        RecommendationEngine engine = RecommendationEngine.getInstance();
        List<Playlist> mixes = engine.getCustomPlaylists(5);

        // Lo stub restituisce 3 generi e 2 decenni (se limit lo permette)
        // Quindi mi aspetto che vengano generate un totale di playlist corrispondente
        assertNotNull(mixes);
        assertFalse(mixes.isEmpty(), "La lista dei mix non deve essere vuota");
        
        // Test di formattazione nome
        boolean hasDecadeMix = mixes.stream().anyMatch(p -> p.getName().contains("anni '90s"));
        boolean hasGenreMix = mixes.stream().anyMatch(p -> p.getName().contains("Mix Rock"));
        
        assertTrue(hasDecadeMix, "Deve generare i titoli dei decenni correttamente");
        assertTrue(hasGenreMix, "Deve generare i titoli dei generi correttamente");
        
        // Verifica ID virtuale
        assertEquals(-1, mixes.get(0).getId());
    }
}