/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.strategy;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Fabio 
 */
public class ShuffleStrategyTest {

    private ShuffleStrategy strategy;
    private List<Track> queue;

    @BeforeEach
    public void setUp() {
        strategy = new ShuffleStrategy();
        queue = Arrays.asList(
            new Track("Traccia 1", "A", 180, "Album", 2000, "Pop", ""),
            new Track("Traccia 2", "B", 180, "Album", 2000, "Rock", ""),
            new Track("Traccia 3", "C", 180, "Album", 2000, "Jazz", ""),
            new Track("Traccia 4", "D", 180, "Album", 2000, "Metal", "")
        );
    }

    @Test
    public void testGetNextTrack_AllTracksPlayed() {
        Set<String> playedTitles = new HashSet<>();
        
        int currentIndex = -1; 
        for (int i = 0; i < queue.size(); i++) {
            Track next = strategy.getNextTrack(queue, currentIndex);
            assertNotNull(next, "Lo shuffle non dovrebbe restituire null prima di aver finito la coda");
            playedTitles.add(next.getTitle());
            
            currentIndex = queue.indexOf(next);
        }

        assertEquals(4, playedTitles.size(), "Deve suonare tutti e 4 i brani distinti senza saltarne o ripeterne nessuno");
    }
}