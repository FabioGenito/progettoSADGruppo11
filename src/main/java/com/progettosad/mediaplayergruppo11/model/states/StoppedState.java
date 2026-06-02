/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.states;

import com.progettosad.mediaplayergruppo11.model.states.PlayerState;
import com.progettosad.mediaplayergruppo11.model.states.PlayingState;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;

/**Stato in cui il lettore è completamente fermo. 
 * da qui è possibile solo passare alla riproduzione
 *
 * @author irene
 */

public class StoppedState implements PlayerState{
    @Override
    public void play(PlaybackEngine context){
        context.setCurrentState(new PlayingState());
        context.getTimeLine().play();
    }
    
    @Override
    //siccome il lettore se fermo non può essere in pausa non fa nessuna operazione
    public void pause(PlaybackEngine context){
        
    }
    
    @Override
    //siccome il lettore è gia fermo non esegue nessuna operazione
    public void stop(PlaybackEngine context){
        
    }
}
