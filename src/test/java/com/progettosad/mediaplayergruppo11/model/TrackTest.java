package com.progettosad.mediaplayergruppo11.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per il modello Track.
 * Verifica l'istanziazione, il passaggio dei parametri e le logiche custom.
 */
class TrackTest {

    @Test
    @DisplayName("Il costruttore parametrizzato dovrebbe inizializzare correttamente tutti i campi")
    void testParameterizedConstructor() {
        String title = "Bohemian Rhapsody";
        String artist = "Queen";
        int length = 354;
        String album = "A Night at the Opera";
        int publicationYear = 1975;
        String genre = "Rock";
        String image = "bohemian.png";
        
        Track track = new Track(title, artist, length, album, publicationYear, genre, image);

        assertEquals(title, track.getTitle());
        assertEquals(artist, track.getArtist());
        assertEquals(length, track.getLength());
        assertEquals(album, track.getAlbum());
        assertEquals(publicationYear, track.getPublicationYear());
        assertEquals(genre, track.getGenre());
        assertEquals(image, track.getImage());
        // L'ID non è nel costruttore, di default Java inizializza gli interi a 0
        assertEquals(0, track.getId()); 
    }

    @Test
    @DisplayName("Il costruttore vuoto e i setter dovrebbero aggiornare lo stato dell'oggetto")
    void testDefaultConstructorAndSetters() {
        // Arrange
        Track track = new Track(); // Utilizzo del costruttore vuoto per la Reflection/DB

        track.setId(10);
        track.setTitle("Pictures of You");
        track.setArtist("The Cure"); // Un piccolo omaggio a Disintegration!
        track.setLength(448);

        assertEquals(10, track.getId());
        assertEquals("Pictures of You", track.getTitle());
        assertEquals("The Cure", track.getArtist());
        assertEquals(448, track.getLength());
        // I campi non esplicitamente settati (oggetti String) dovrebbero essere null
        assertNull(track.getAlbum()); 
    }

    @Test
    @DisplayName("getFormattedLength dovrebbe restituire la stringa di tempo formattata correttamente")
    void testGetFormattedLength() {
  
        Track track = new Track();
        
        track.setLength(125); 
        assertEquals("02:05", track.getFormattedLength(), "125 secondi dovrebbero diventare 02:05");

        //caso di una traccia senza durata (es. stream live o errore DB)
        track.setLength(0);
        assertEquals("00:00", track.getFormattedLength(), "0 secondi dovrebbero diventare 00:00");
    }
}