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

    private PlaybackEngine engine;
    
    /**
     * Accende il motore interno di JavaFX senza interfaccia.
     * È necessario per poter testare classi che usano Timeline o Property.
     */
    @BeforeAll
    static void initJavaFXToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit già avviato, ignora l'eccezione
        }
    }
    
    /**
     * Resetta il Singleton e ottiene una nuova istanza prima di OGNI test.
     * Garantisce il principio di Isolamento dei Test.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 1. Distrugge l'istanza corrente del Singleton tramite Reflection
        Field instanceField = PlaybackEngine.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null); 

        // 2. Assegna la nuova istanza pulita al campo della classe di test
        this.engine = PlaybackEngine.getInstance();
    }

    /**
     * Metodo helper per creare una playlist di test popolata.
     */
    private List<Track> createMockPlaylist(int size) {
        List<Track> playlist = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Track t = new Track("Track " + i, "Artist", 100, "Album", 2000, "Pop", "");
            t.setId(i);
            playlist.add(t);
        }
        return playlist;
    }

    /**
     * Metodo di supporto unico per leggere il currentIndex dall'iteratore privato.
     */
    private int getIteratorCurrentIndex() throws Exception {
        Field iteratorField = PlaybackEngine.class.getDeclaredField("playlistIterator");
        iteratorField.setAccessible(true);
        Object iterator = iteratorField.get(engine);
        
        if (iterator == null) {
            fail("playlistIterator è null! Assicurati di chiamare playSelection() nel test per inizializzarlo.");
        }
        
        // Estrae il campo 'currentIndex' basandosi sulle chiamate dell'Engine
        Field indexField = iterator.getClass().getDeclaredField("currentIndex");
        indexField.setAccessible(true);
        return (int) indexField.get(iterator);
    }

    @Test
    @DisplayName("playSelection dovrebbe lanciare eccezione se riceve una lista vuota o nulla (Gestione Errori)")
    void testPlaySelection_EmptyOrNullPlaylist() {
        List<Track> emptyList = new ArrayList<>();

        assertThrows(IllegalStateException.class, () -> {
            engine.playSelection(null, emptyList);
        }, "Dovrebbe lanciare IllegalStateException se la playlist è vuota");

        assertThrows(IllegalStateException.class, () -> {
            engine.playSelection(null, null);
        }, "Dovrebbe lanciare IllegalStateException se la playlist è nulla");
    }

    @Test
    @DisplayName("playSelection dovrebbe avviare la prima traccia se non c'è selezione esplicita (Regola di Dominio Fallback)")
    void testPlaySelection_NullSelectionPlaysFirst() {
        List<Track> playlist = createMockPlaylist(2);

        // Passo 'null' come traccia selezionata
        engine.playSelection(null, playlist);

        assertEquals(playlist.get(0), engine.currentTrackProperty().get(), "Dovrebbe aver caricato la prima traccia della lista");
        assertTrue(engine.isPlayingProperty().get(), "Il motore dovrebbe risultare in riproduzione");
    }

    @Test
    @DisplayName("playTrack dovrebbe ignorare input null senza causare NullPointerException (Robustezza)")
    void testPlayTrack_NullInput() {
        engine.playTrack(null);
        
        assertNull(engine.currentTrackProperty().get());
        falseFalse(engine.isPlayingProperty().get());
    }

    private void falseFalse(boolean condition) {
        assertFalse(condition);
    }

    @Test
    @DisplayName("stopTrack dovrebbe azzerare il tempo, il progresso e lo stato di riproduzione")
    void testStopTrack_ResetsProgress() {
        Track track = new Track("Title", "Artist", 120, "Album", 2000, "Rock", "img.png");
        track.setId(5);

        engine.playTrack(track);
        engine.setCurrentTime(60); 
        engine.progressProperty().set(0.5);

        engine.stopTrack();

        assertFalse(engine.isPlayingProperty().get());
        assertEquals(0, engine.currentTimeProperty().get());
        assertEquals(0.0, engine.progressProperty().get());
    }
    
    @Test
    @DisplayName("playTrack dovrebbe riprendere la riproduzione senza azzerare i contatori se è la stessa traccia in pausa")
    void testPlayTrack_ResumePausedTrack() {
        Track track = new Track("Lullaby", "The Cure", 240, "Disintegration", 1989, "Post-Punk", "img.png");
        track.setId(10);

        engine.playTrack(track);
        engine.setCurrentTime(50); 
        
        engine.pauseTrack();
        assertFalse(engine.isPlayingProperty().get(), "Il motore deve risultare in pausa");
        
        engine.playTrack(track);
        
        assertTrue(engine.isPlayingProperty().get(), "Il motore deve essere tornato in riproduzione");
        assertEquals(50, engine.currentTimeProperty().get(), "Il tempo non deve essere stato azzerato");
    }

    @Test
    @DisplayName("setPlaybackStrategy dovrebbe aggiornare correttamente la strategia corrente")
    void testSetPlaybackStrategy_UpdatesStrategy() {
        assertTrue(engine.getPlaybackStrategy() instanceof com.progettosad.mediaplayergruppo11.model.strategy.SequentialStrategy);
        
        engine.setPlaybackStrategy(new com.progettosad.mediaplayergruppo11.model.strategy.LoopStrategy());
        
        assertTrue(engine.getPlaybackStrategy() instanceof com.progettosad.mediaplayergruppo11.model.strategy.LoopStrategy, 
            "La strategia attiva deve riflettere l'ultima iniettata tramite setter");
    }

    @Test
    @DisplayName("moveTrackInQueue dovrebbe aggiornare correttamente l'indice se la traccia attiva viene scavalcata")
    void testMoveTrackInQueue_ShiftsCurrentIndex() throws Exception {
        List<Track> playlist = createMockPlaylist(3);
        engine.playSelection(playlist.get(1), playlist);
        
        // Spostiamo la traccia 0 (prima di quella attiva) all'indice 2 (dopo quella attiva)
        engine.moveTrackInQueue(0, 2);
        
        int updatedIndex = getIteratorCurrentIndex();
        assertEquals(0, updatedIndex, "L'indice corrente deve decrementare se un brano precedente viene spostato in avanti");
    }

    @Test
    @DisplayName("moveTrackInQueue dovrebbe tracciare la traccia attiva se viene mossa direttamente")
    void testMoveTrackInQueue_ActiveTrackMoved() throws Exception {
        List<Track> playlist = createMockPlaylist(3);
        engine.playSelection(playlist.get(0), playlist);       

        engine.moveTrackInQueue(0, 2);
        
        int updatedIndex = getIteratorCurrentIndex();
        assertEquals(2, updatedIndex, "Se viene spostata la traccia in riproduzione, l'indice corrente deve seguirla nella nuova posizione");
    }
    
    @Test
    @DisplayName("Caso 1: oldIndex == currentTrackIndex -> il nuovo indice diventa newIndex")
    void testMoveCurrentlyPlayingTrack() throws Exception {
        List<Track> playlist = createMockPlaylist(5);
        // Avviamo la riproduzione impostando l'indice corrente a 2
        engine.playSelection(playlist.get(2), playlist);

        // Il brano in riproduzione è all'indice 2. Lo spostiamo all'indice 4.
        engine.moveTrackInQueue(2, 4);
        
        int updatedIndex = getIteratorCurrentIndex();
        assertEquals(4, updatedIndex, "Il currentTrackIndex dovrebbe seguire il brano spostato");
        assertTrue(engine.isPlayingProperty().get(), "La riproduzione non deve interrompersi");
    }

    @Test
    @DisplayName("Caso 2: oldIndex < currentTrackIndex E newIndex >= currentTrackIndex -> decrementa di 1")
    void testMoveTrackFromBeforeToAfterCurrent() throws Exception {
        List<Track> playlist = createMockPlaylist(5);
        engine.playSelection(playlist.get(2), playlist);

        // Spostiamo il brano dall'indice 0 all'indice 3.
        engine.moveTrackInQueue(0, 3);
        
        int updatedIndex = getIteratorCurrentIndex();
        assertEquals(1, updatedIndex, "Il currentTrackIndex dovrebbe decrementare di 1");
    }

    @Test
    @DisplayName("Caso 3: oldIndex > currentTrackIndex E newIndex <= currentTrackIndex -> incrementa di 1")
    void testMoveTrackFromAfterToBeforeCurrent() throws Exception {
        List<Track> playlist = createMockPlaylist(5);
        engine.playSelection(playlist.get(2), playlist);

        // Spostiamo il brano dall'indice 4 all'indice 1.
        engine.moveTrackInQueue(4, 1);
        
        int updatedIndex = getIteratorCurrentIndex();
        assertEquals(3, updatedIndex, "Il currentTrackIndex dovrebbe incrementare di 1");
    }

    @Test
    @DisplayName("Caso 4: Indici non validi o uguali (Edge Cases)")
    void testMoveTrackInvalidIndices() throws Exception {
        List<Track> playlist = createMockPlaylist(5);
        engine.playSelection(playlist.get(2), playlist);
        
        int initialIndex = getIteratorCurrentIndex();
        
        // Spostamento nullo (stesso indice)
        engine.moveTrackInQueue(2, 2);
        assertEquals(initialIndex, getIteratorCurrentIndex());
        
        // Indice fuori dai limiti inferiori
        engine.moveTrackInQueue(-1, 3);
        assertEquals(initialIndex, getIteratorCurrentIndex());
        
        // Indice fuori dai limiti superiori
        engine.moveTrackInQueue(2, 10);
        assertEquals(initialIndex, getIteratorCurrentIndex());
    }
}