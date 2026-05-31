package com.progettosad.mediaplayergruppo11.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.progettosad.mediaplayergruppo11.db.DatabaseManager;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;

public class PlaylistDAO {

    /**
     * Associa una traccia a una playlist all'interno della tabella playlist_tracks.
     * * @param playlistId ID della playlist
     * @param trackId ID della traccia
     * @return true se l'inserimento ha successo
     * @throws TrackAlreadyInPlaylistException se la traccia è già presente nella playlist
     * @throws RuntimeException in caso di altri errori SQL o di connessione
     */
    public boolean addTrackToPlaylist(int playlistId, int trackId) {
        String sql = "INSERT INTO playlist_tracks (playlist_id, track_id) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, trackId);

            int affectedRows = pstmt.executeUpdate();
            
            // Restituisce true se esattamente una riga è stata inserita
            return affectedRows == 1;

        } catch (SQLException e) {
            // "23505" è il codice di stato PostgreSQL per "UNIQUE_VIOLATION"
            // Si verifica se la coppia (playlist_id, track_id) esiste già (Chiave Primaria o Vincolo Unique)
            if ("23505".equals(e.getSQLState())) {
                throw new TrackAlreadyInPlaylistException(
                    "La traccia con ID " + trackId + " è già presente nella playlist con ID " + playlistId
                );
            }
            
            // Per qualsiasi altro errore SQL
            throw new RuntimeException("Errore SQL durante l'aggiunta della traccia alla playlist", e);
        }
    }
    
    /**
     * Rimuove l'associazione tra una traccia e una playlist.
     * @param playlistId ID della playlist
     * @param trackId ID della traccia da rimuovere
     * @return true se la rimozione ha successo (una riga eliminata), false altrimenti
     */
    public boolean removeTrackFromPlaylist(int playlistId, int trackId) {
        String sql = "DELETE FROM playlist_tracks WHERE playlist_id = ? AND track_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, trackId);

            int affectedRows = pstmt.executeUpdate();
            
            // Restituisce true rigorosamente solo se esattamente un record è stato eliminato
            return affectedRows == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante la rimozione della traccia dalla playlist", e);
        }
    }
}