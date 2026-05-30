package com.progettosad.mediaplayergruppo11.exception;

/**
 * Eccezione personalizzata lanciata quando si tenta di aggiungere 
 * una traccia a una playlist in cui è già presente.
 */
public class TrackAlreadyInPlaylistException extends RuntimeException {
    
    public TrackAlreadyInPlaylistException(String message) {
        super(message);
    }
}