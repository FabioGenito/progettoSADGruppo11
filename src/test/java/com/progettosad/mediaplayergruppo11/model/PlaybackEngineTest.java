package com.progettosad.mediaplayergruppo11.model;

import javafx.application.Platform;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per PlaybackEngine.
 * Verifica la logica di fallback, la gestione degli stati e la robustezza.
 */
class PlaybackEngineTest {

    /**
     * Accende il motore interno di JavaFX senza interfaccia.
     * È necessario per poter testare classi che usano Timeline o Property.
     */
    @BeforeAll
    static void initJavaFXToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Resetta il Singleton prima di ogni test.
     * PlaybackEngine è un Singleton, e per garantire il principio di Isolamento dei Test
     * è necessario distruggere l'istanza tra un test e l'altro.
     */
    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = PlaybackEngine.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null); // Distrugge l'istanza corrente
    }

    @Test
    @DisplayName("playSelection dovrebbe lanciare eccezione se riceve una lista vuota o nulla (Gestione Errori)")
    void testPlaySelection_EmptyOrNullPlaylist() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        List<Track> emptyList = new ArrayList<>();

        // Verifica lista vuota
        assertThrows(IllegalStateException.class, () -> {
            engine.playSelection(null, emptyList);
        }, "Dovrebbe lanciare IllegalStateException se la playlist è vuota");

        // Verifica lista nulla
        assertThrows(IllegalStateException.class, () -> {
            engine.playSelection(null, null);
        }, "Dovrebbe lanciare IllegalStateException se la playlist è nulla");
    }

    @Test
    @DisplayName("playSelection dovrebbe avviare la prima traccia se non c'è selezione esplicita (Regola di Dominio Fallback)")
    void testPlaySelection_NullSelectionPlaysFirst() {
       
        PlaybackEngine engine = PlaybackEngine.getInstance();
        List<Track> playlist = new ArrayList<>();
        Track firstTrack = new Track("Lullaby", "The Cure", 240, "Disintegration", 1989, "Post-Punk", "img.png");
        firstTrack.setId(1);
        Track secondTrack = new Track("Fascination Street", "The Cure", 310, "Disintegration", 1989, "Post-Punk", "img.png");
        secondTrack.setId(2);
        
        playlist.add(firstTrack);
        playlist.add(secondTrack);

        //Passo 'null' come traccia selezionata
        engine.playSelection(null, playlist);

        assertEquals(firstTrack, engine.currentTrackProperty().get(), "Dovrebbe aver caricato la prima traccia della lista");
        assertTrue(engine.isPlayingProperty().get(), "Il motore dovrebbe risultare in riproduzione");
    }

    @Test
    @DisplayName("playTrack dovrebbe ignorare input null senza causare NullPointerException (Robustezza)")
    void testPlayTrack_NullInput() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        
        engine.playTrack(null);
        
        //Lo stato deve rimanere inalterato
        assertNull(engine.currentTrackProperty().get());
        assertFalse(engine.isPlayingProperty().get());
    }

    @Test
    @DisplayName("stopTrack dovrebbe azzerare il tempo, il progresso e lo stato di riproduzione")
    void testStopTrack_ResetsProgress() {
       
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track track = new Track("Title", "Artist", 120, "Album", 2000, "Rock", "img.png");
        track.setId(5);

        engine.playTrack(track);
        engine.setCurrentTime(60); // Simuliamo che il brano sia arrivato a metà
        engine.progressProperty().set(0.5);

        engine.stopTrack();

        assertFalse(engine.isPlayingProperty().get());
        assertEquals(0, engine.currentTimeProperty().get());
        assertEquals(0.0, engine.progressProperty().get());
    }
    
    @Test
    @DisplayName("playTrack dovrebbe riprendere la riproduzione senza azzerare i contatori se è la stessa traccia in pausa")
    void testPlayTrack_ResumePausedTrack() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track track = new Track("Lullaby", "The Cure", 240, "Disintegration", 1989, "Post-Punk", "img.png");
        track.setId(10);

        engine.playTrack(track);
        engine.setCurrentTime(50); // Simuliamo che l'utente abbia ascoltato 50 secondi
        
        engine.pauseTrack();
        assertFalse(engine.isPlayingProperty().get(), "Il motore deve risultare in pausa");
        
        // L'utente preme di nuovo Play
        engine.playTrack(track);
        
        assertTrue(engine.isPlayingProperty().get(), "Il motore deve essere tornato in riproduzione");
        assertEquals(50, engine.currentTimeProperty().get(), "Il tempo non deve essere stato azzerato");
    }

    @Test
    @DisplayName("setPlaybackStrategy dovrebbe aggiornare correttamente la strategia corrente")
    void testSetPlaybackStrategy_UpdatesStrategy() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        
        // Di default dovrebbe essere SequentialStrategy
        assertTrue(engine.getPlaybackStrategy() instanceof com.progettosad.mediaplayergruppo11.model.strategy.SequentialStrategy);
        
        // Iniezione di una nuova strategia
        engine.setPlaybackStrategy(new com.progettosad.mediaplayergruppo11.model.strategy.LoopStrategy());
        
        assertTrue(engine.getPlaybackStrategy() instanceof com.progettosad.mediaplayergruppo11.model.strategy.LoopStrategy, 
            "La strategia attiva deve riflettere l'ultima iniettata tramite setter");
    }

    @Test
    @DisplayName("moveTrackInQueue dovrebbe aggiornare correttamente l'indice se la traccia attiva viene scavalcata")
    void testMoveTrackInQueue_ShiftsCurrentIndex() throws Exception {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        List<Track> playlist = new ArrayList<>();
        playlist.add(new Track("Track 0", "A", 100, "A", 2000, "Pop", ""));
        playlist.add(new Track("Track 1", "B", 100, "B", 2000, "Pop", ""));
        playlist.add(new Track("Track 2", "C", 100, "C", 2000, "Pop", ""));
        
        engine.playSelection(playlist.get(1), playlist);
        
        // Spostiamo la traccia 0 (prima di quella attiva) all'indice 2 (dopo quella attiva)
        // La traccia attiva (Track 1) scala dall'indice 1 all'indice 0.
        engine.moveTrackInQueue(0, 2);
        
        // Usiamo la Reflection per leggere l'indice interno protetto dell'iteratore
        int updatedIndex = getIteratorCurrentIndex(engine);
        assertEquals(0, updatedIndex, "L'indice corrente deve decrementare se un brano precedente viene spostato in avanti");
    }

    @Test
    @DisplayName("moveTrackInQueue dovrebbe tracciare la traccia attiva se viene mossa direttamente")
    void testMoveTrackInQueue_ActiveTrackMoved() throws Exception {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        List<Track> playlist = new ArrayList<>();
        playlist.add(new Track("Track 0", "A", 100, "A", 2000, "Pop", ""));
        playlist.add(new Track("Track 1", "B", 100, "B", 2000, "Pop", "")); 
        playlist.add(new Track("Track 2", "C", 100, "C", 2000, "Pop", ""));
        
        engine.playSelection(playlist.get(0), playlist);       
        // L'utente prende la traccia in riproduzione e la trascina in fondo (indice 2)
        engine.moveTrackInQueue(0, 2);
        
        int updatedIndex = getIteratorCurrentIndex(engine);
        assertEquals(2, updatedIndex, "Se viene spostata la traccia in riproduzione, l'indice corrente deve seguirla nella nuova posizione");
    }

    /**
     * Metodo di utilità per testare moveTrackInQueue leggendo lo stato protetto dell'iteratore.
     */
    private int getIteratorCurrentIndex(PlaybackEngine engine) throws Exception {
        Field iteratorField = PlaybackEngine.class.getDeclaredField("playlistIterator");
        iteratorField.setAccessible(true);
        Object iterator = iteratorField.get(engine);
        
        if (iterator == null) return -1;
        
        Field indexField = iterator.getClass().getDeclaredField("currentIndex");
        indexField.setAccessible(true);
        return (int) indexField.get(iterator);
    }
}