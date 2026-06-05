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
 * T-11/03: strategia di riproduzione a ciclo continuo(loop)
 * Quando la coda termina ritorna al primo brano.
 */
public class LoopStrategy implements PlaybackStrategy{
    @Override
    public Track getNextTrack(List<Track> queue, int currentIndex){
       if(queue==null || queue.isEmpty()){
           return null;
       } 
       if (currentIndex >=0 && currentIndex < queue.size()){
           return queue.get(currentIndex);
       }
       return queue.get(0);
    }
}
