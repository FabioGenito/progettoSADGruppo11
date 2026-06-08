/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.model.strategy.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Fabio
 */
public class ConcretePlaylistIteratorTest {

    private ConcretePlaylistIterator iterator;
    private List<Track> queue;
    private PlaybackEngine engine;

    @BeforeEach
    public void setUp() {
        engine = PlaybackEngine.getInstance();
        queue = Arrays.asList(
            new Track("Traccia 1", "A", 180, "Al", 2000, "Pop", ""),
            new Track("Traccia 2", "B", 180, "Al", 2000, "Rock", "")
        );
        iterator = new ConcretePlaylistIterator(queue, 0); 
    }

    @Test
    public void testIterator_SequentialFlow() {
        engine.setPlaybackStrategy(new SequentialStrategy());
        assertTrue(iterator.hasNext(), "In modalità sequenziale, avendo 2 brani, hasNext deve essere true");
        
        Track nextTrack = iterator.next(); 
        assertEquals("Traccia 2", nextTrack.getTitle());
        
        assertFalse(iterator.hasNext(), "In modalità sequenziale, dopo l'ultimo brano hasNext() deve essere false");
    }

    @Test
    public void testIterator_DynamicStrategyChange_SingleLoop() {
        // Partiamo in sequenziale
        engine.setPlaybackStrategy(new SequentialStrategy());
        
        // Simuliamo il passaggio all'ultima traccia
        iterator.next(); 
        
        // Il motore direbbe che è finita
        assertFalse(iterator.hasNext()); 
        
        // L'utente clicca il bottone "Loop" sulla UI -> Il controller aggiorna il motore!
        engine.setPlaybackStrategy(new LoopStrategy());
        
        // Pattern Strategy: l'iteratore legge la nuova strategia dal motore
        assertTrue(iterator.hasNext(), "Cambiando in LoopStrategy, hasNext() deve tornare true anche a fine coda");
        
        // Il loop singolo restituisce ininterrottamente il brano attualmente attivo
        Track loopedTrack = iterator.next();
        assertEquals("Traccia 2", loopedTrack.getTitle(), "Il loop deve mantenere in riproduzione lo stesso brano corrente");
    }
}