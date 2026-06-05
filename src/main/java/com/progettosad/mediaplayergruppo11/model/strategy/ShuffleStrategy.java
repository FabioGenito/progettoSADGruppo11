/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.strategy;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author irene
 * T-11/03: Strategia di riproduzione casuale.
 * Genera internamente una coda rimescolata e la percorre sequenzialmente.
 */
public class ShuffleStrategy implements PlaybackStrategy {
    
    private List<Track> shuffledQueue;

    @Override
    public Track getNextTrack(List<Track> queue, int currentIndex) {
        if (queue == null || queue.isEmpty()) return null;

        // Se la coda rimescolata non esiste, o se la playlist sorgente è cambiata
        if (shuffledQueue == null || shuffledQueue.size() != queue.size() || !shuffledQueue.containsAll(queue)) {
            shuffledQueue = new ArrayList<>(queue);
            Collections.shuffle(shuffledQueue); 
        }

        // Se nessun brano è in esecuzione (currentIndex < 0), inizia dal primo brano della coda rimescolata
        if (currentIndex < 0 || currentIndex >= queue.size()) {
            return shuffledQueue.get(0);
        }

        Track currentTrack = queue.get(currentIndex);
        int indexInShuffled = shuffledQueue.indexOf(currentTrack);

        
        if (indexInShuffled != -1 && indexInShuffled < shuffledQueue.size() - 1) {
            return shuffledQueue.get(indexInShuffled + 1);
        }

        return null; 
    } 
} 