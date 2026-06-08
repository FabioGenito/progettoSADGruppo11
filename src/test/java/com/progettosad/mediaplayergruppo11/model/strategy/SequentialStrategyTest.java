/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.strategy;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Fabio
 */
public class SequentialStrategyTest {

    private SequentialStrategy strategy;
    private List<Track> queue;

    @BeforeEach
    public void setUp() {
        strategy = new SequentialStrategy();
        queue = Arrays.asList(
            new Track("Traccia 1", "A", 180, "Album", 2000, "Pop", ""),
            new Track("Traccia 2", "B", 180, "Album", 2000, "Rock", ""),
            new Track("Traccia 3", "C", 180, "Album", 2000, "Jazz", "")
        );
    }

    @Test
    public void testGetNextTrack_NormalProgression() {
        // Dalla traccia 0 mi aspetto la traccia 1
        Track next = strategy.getNextTrack(queue, 0);
        assertEquals("Traccia 2", next.getTitle(), "Deve restituire la traccia successiva nell'elenco");
    }

    @Test
    public void testGetNextTrack_EndOfQueue() {
        // Dalla traccia 2 (l'ultima), la coda sequenziale finisce
        Track next = strategy.getNextTrack(queue, 2);
        assertNull(next, "Se la coda è finita in modalità sequenziale, deve restituire null");
    }
}