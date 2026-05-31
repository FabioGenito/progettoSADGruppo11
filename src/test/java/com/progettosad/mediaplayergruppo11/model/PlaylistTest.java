package com.progettosad.mediaplayergruppo11.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per il modello Playlist.
 * Verifica l'istanziazione e l'override dei metodi standard.
 */
class PlaylistTest {

    @Test
    @DisplayName("Il costruttore parametrizzato dovrebbe inizializzare correttamente i campi")
    void testParameterizedConstructor() {
        
        int id = 1;
        String name = "Rock Classics";
        String image = "rock_cover.png";

        Playlist playlist = new Playlist(id, name, image);

        
        assertEquals(id, playlist.getId());
        assertEquals(name, playlist.getName());
        assertEquals(image, playlist.getImage());
    }

    @Test
    @DisplayName("Il costruttore vuoto e i setter dovrebbero aggiornare lo stato dell'oggetto")
    void testDefaultConstructorAndSetters() {
        
        Playlist playlist = new Playlist();

        playlist.setId(42);
        playlist.setName("Studio Vibes");
        playlist.setImage("studio.jpg");
        
        assertEquals(42, playlist.getId());
        assertEquals("Studio Vibes", playlist.getName());
        assertEquals("studio.jpg", playlist.getImage());
    }

    @Test
    @DisplayName("Il metodo toString dovrebbe restituire esclusivamente il nome della playlist per la UI")
    void testToStringOverride() {
        
        Playlist playlist = new Playlist(5, "Indie Mix", "indie.png");

        String stringRepresentation = playlist.toString();

        assertEquals("Indie Mix", stringRepresentation, 
            "Il toString() deve restituire il nome per permettere a JavaFX di visualizzarlo correttamente");
    }
}