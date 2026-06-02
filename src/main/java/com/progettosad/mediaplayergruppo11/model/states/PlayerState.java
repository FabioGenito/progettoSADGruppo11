/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author irene
 * 
 * interfaccia comune per il pattern State.
 * Stati del lettore musicale evitando costrutti if/switch delegando la logica alle classi
 */

package com.progettosad.mediaplayergruppo11.model.states;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;

public interface PlayerState {
    void play(PlaybackEngine context);
    void pause(PlaybackEngine context);
    void stop(PlaybackEngine context);
}
