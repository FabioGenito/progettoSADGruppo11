/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.observer;

/**
 * Oggetto che incapsula le informazioni di un cambiamento di stato.
 * @author Fabio
 */

public class AppEvent {
    private final AppEventType type;
    private final Object payload; // Può essere l'ID della traccia, l'oggetto Playlist, o un intero

    public AppEvent(AppEventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public AppEventType getType() { return type; }
    public Object getPayload() { return payload; }
}
