/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model.states;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;

/**
 *
 * @author irene
 */
public class PausedState implements PlayerState{
    @Override
    public void play(PlaybackEngine context){
        context.setCurrentState(new PlayingState());
    }
    
    @Override
    public void pause(PlaybackEngine context){
        // Già in pausa, non fa nulla
    }
    
    @Override
    public void stop(PlaybackEngine context){
        context.setCurrentState(new StoppedState());
    }
}
