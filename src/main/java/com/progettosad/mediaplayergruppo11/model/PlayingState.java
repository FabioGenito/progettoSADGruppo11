/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

/**
 *
 * @author irene
 * Stato in cui il lettore sta eseguendo un brano.
 * Qui vengono gestiti l'interruzione temporanea o definitiva della Timeline
 */
public class PlayingState implements PlayerState{
    @Override
    //caso in cui la traccia sia già in riproduzione non fa nulla
    public void play(PlaybackEngine context){
        
    }
    
    //se viene premuto il pulsante pausa il sistema salva il tempo in cui
    //la canzone viene stoppata e setta lo stato del sistema all'istante di pausa
    //sospendendo l'avanzamento
    @Override
    public void pause(PlaybackEngine context){
        context.getTimeLine().pause();
        context.setCurrentState(new PausedState());
    }
    
    //se viene premuto il pulsante stop il sistema stoppa la ripoduzione
    //settando la line di nuovo a 0.
    @Override
    public void stop(PlaybackEngine context){
        context.getTimeLine().stop();
        context.setCurrentTime(0);
        context.setCurrentState(new StoppedState());
    }
    
    
}
