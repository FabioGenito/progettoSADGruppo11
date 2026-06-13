package com.progettosad.mediaplayergruppo11.command;

import com.progettosad.mediaplayergruppo11.dao.FakePlaylistDAO;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddTrackCommandTest {

    private FakePlaylistDAO fakeDAO;
    private static final int PLAYLIST_ID = 1;
    private static final int TRACK_ID = 42;

    @BeforeEach
    void setUp() {
        this.fakeDAO = new FakePlaylistDAO();
        // Creiamo una playlist fittizia per avere un contesto valido
        fakeDAO.createPlaylist("Test Playlist", "cover.png");
    }

    // ------------------------------------------------------------------ //
    //  EXECUTE
    // ------------------------------------------------------------------ //

    @Test
    void testExecuteAddsTrackSuccessfully() {
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);

        // Non deve lanciare eccezioni
        assertDoesNotThrow(cmd::execute);

        // Verifica che la traccia sia effettivamente presente
        // (rimuovendola: removeTrackFromPlaylist restituisce true se esisteva)
        assertTrue(fakeDAO.removeTrackFromPlaylist(PLAYLIST_ID, TRACK_ID),
                "La traccia dovrebbe essere stata aggiunta dal comando");
    }

    @Test
    void testExecuteThrowsWhenTrackAlreadyInPlaylist() {
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);

        // Prima aggiunta: ok
        cmd.execute();

        // Seconda aggiunta: deve rilanciare l'eccezione
        assertThrows(TrackAlreadyInPlaylistException.class, cmd::execute,
                "La seconda aggiunta della stessa traccia deve lanciare TrackAlreadyInPlaylistException");
    }

    @Test
    void testExecuteAddsDifferentTracksToSamePlaylist() {
        AddTrackCommand cmd1 = new AddTrackCommand(TRACK_ID,      PLAYLIST_ID, fakeDAO);
        AddTrackCommand cmd2 = new AddTrackCommand(TRACK_ID + 1,  PLAYLIST_ID, fakeDAO);

        assertDoesNotThrow(cmd1::execute);
        assertDoesNotThrow(cmd2::execute);
    }

    @Test
    void testExecuteAddsSameTrackToDifferentPlaylists() {
        fakeDAO.createPlaylist("Seconda Playlist", "cover2.png");
        int secondPlaylistId = 2;

        AddTrackCommand cmd1 = new AddTrackCommand(TRACK_ID, PLAYLIST_ID,     fakeDAO);
        AddTrackCommand cmd2 = new AddTrackCommand(TRACK_ID, secondPlaylistId, fakeDAO);

        assertDoesNotThrow(cmd1::execute);
        assertDoesNotThrow(cmd2::execute);
    }

    // ------------------------------------------------------------------ //
    //  UNDO
    // ------------------------------------------------------------------ //

    @Test
    void testUndoRemovesTrackAddedByExecute() {
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);
        cmd.execute();

        // L'undo non deve lanciare eccezioni
        assertDoesNotThrow(cmd::undo);

        // Dopo l'undo la traccia non c'è più: una nuova execute() deve riuscire
        assertDoesNotThrow(cmd::execute,
                "Dopo undo() la traccia è stata rimossa: una nuova execute() deve riuscire");
    }

    @Test
    void testUndoOnNonExistentTrackDoesNotThrow() {
        // Non chiamiamo execute(): la traccia non è mai stata aggiunta
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);

        assertDoesNotThrow(cmd::undo,
                "undo() su una traccia mai aggiunta non deve propagare eccezioni");
    }

    @Test
    void testExecuteUndoExecuteCycle() {
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);

        cmd.execute();  // Aggiunge
        cmd.undo();     // Rimuove
        cmd.execute();  //non deve lanciare TrackAlreadyInPlaylistException

        //la traccia è presente
        assertTrue(fakeDAO.removeTrackFromPlaylist(PLAYLIST_ID, TRACK_ID));
    }

    @Test
    void testUndoAfterDuplicateExecuteIsRobust() {
        AddTrackCommand cmd = new AddTrackCommand(TRACK_ID, PLAYLIST_ID, fakeDAO);

        cmd.execute(); //Aggiunge correttamente

        // La seconda chiamata lancia l'eccezione, ma undo() non deve essere compromesso
        assertThrows(TrackAlreadyInPlaylistException.class, cmd::execute);
        assertDoesNotThrow(cmd::undo);
    }
}