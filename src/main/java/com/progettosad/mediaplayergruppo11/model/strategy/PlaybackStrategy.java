/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.strategy;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;

/** T-11/01
 *Definisce la strategia per la determinazione del brano successivo nella coda di riproduzione
 * @author irene
 */
public interface PlaybackStrategy {
    //Calcola e restituisce il prossimo brano da riprodurr..
Track getNextTrack(List<Track> queue, int currentIndex);
}


