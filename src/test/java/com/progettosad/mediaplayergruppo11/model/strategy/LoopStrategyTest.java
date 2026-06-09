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
public class LoopStrategyTest {

    private LoopStrategy strategy;
    private List<Track> queue;

    @BeforeEach
    public void setUp() {
        strategy = new LoopStrategy();
        queue = Arrays.asList(
            new Track("Traccia 1", "A", 180, "Album", 2000, "Pop", ""),
            new Track("Traccia 2", "B", 180, "Album", 2000, "Rock", ""),
            new Track("Traccia 3", "C", 180, "Album", 2000, "Jazz", "")
        );
    }

    @Test
    public void testGetNextTrack_SingleTrackLoop() {
        // Supponiamo che il player stia suonando la "Traccia 2" (indice 1)
        int currentIndex = 1;
        
        // Chiediamo alla strategia quale sarà il prossimo brano
        Track next = strategy.getNextTrack(queue, currentIndex);
        
        assertNotNull(next);
        assertEquals("Traccia 2", next.getTitle(), "In modalità Loop, deve restituire esattamente lo stesso brano attualmente in riproduzione");
        
        // Verifichiamo che lo faccia anche se chiamato più volte di fila
        Track nextAgain = strategy.getNextTrack(queue, currentIndex);
        assertEquals("Traccia 2", nextAgain.getTitle(), "Deve continuare a restituire lo stesso brano all'infinito");
    }
}