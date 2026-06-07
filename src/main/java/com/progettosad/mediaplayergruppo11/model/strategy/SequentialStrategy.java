/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.strategy;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;
/**
 *
 * @author irene
 * T-11/01
 * Strategia di riprouduzione lineare: riproduce i brani in ordine e si ferma alla fine della coda
 */

public class SequentialStrategy implements PlaybackStrategy{
    
    @Override
    public Track getNextTrack(List<Track> queue, int currentIndex){
        if(queue==null || queue.isEmpty()){
            return null;
        }
        int nextIndex=currentIndex +1;
        
        if (nextIndex < queue.size()){
            return queue.get(nextIndex);
        }
        return null;
    }
}
