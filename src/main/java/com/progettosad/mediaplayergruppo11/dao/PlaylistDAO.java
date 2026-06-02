package com.progettosad.mediaplayergruppo11.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.progettosad.mediaplayergruppo11.db.DatabaseManager;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO implements PlaylistDAOInterface {
    
    private static final String INSERT_PLAYLIST = "INSERT INTO playlist(name, image) VALUES (?, ?)";
    private static final String SELECT_ALL_PLAYLISTS = "SELECT id, name, image FROM playlist ORDER BY id ASC";
    private static final String ADD_TRACK_TO_PLAYLIST = "INSERT INTO playlist_tracks (playlist_id, track_id) VALUES (?, ?)";
    private static final String REMOVE_TRACK_FROM_PLAYLIST = "DELETE FROM playlist_tracks WHERE playlist_id = ? AND track_id = ?";
    private static final String SELECT_TRACKS_BY_PLAYLIST = "SELECT t.* FROM Tracks t JOIN playlist_tracks pt ON t.id = pt.track_id WHERE pt.playlist_id = ? ORDER BY t.title ASC";
    
    /**
     * Crea una nuova playlist nel database e ne recupera l'ID generato automaticamente.
     * * @param name  Il nome della playlist (non può essere nullo o vuoto).
     * @param image Il percorso, URL o nome del file dell'immagine di copertina.
     * @return Un oggetto Playlist completo dell'ID appena generato dal database.
     * @throws IllegalArgumentException Se il nome fornito è nullo o vuoto.
     * @throws RuntimeException In caso di errori SQL durante l'inserimento o la generazione dell'ID.
     */
    @Override
    public Playlist createPlaylist(String name, String image) {        

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della Playlist non può essere vuoto");
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_PLAYLIST, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, name);
            statement.setString(2, image); 

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creazione Playlist fallita, nessuna riga inserita.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    return new Playlist(generatedId, name, image);
                } else {
                    throw new SQLException("ID Playlist non generato");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione Playlist nel DB", e);
        }
    }

    /**
     * Estrae l'elenco completo di tutte le playlist salvate nel database.
     * * @return Una lista di oggetti Playlist ordinati in base all'ID. 
     * Se non ci sono playlist, restituisce una lista vuota.
     * @throws RuntimeException In caso di errori SQL o di connettività.
     */
    @Override
    public List<Playlist> getAllPlaylists() {
        List<Playlist> listaPlaylist = new ArrayList<>();
        
        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_PLAYLISTS);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String image = resultSet.getString("image");
                listaPlaylist.add(new Playlist(id, name, image));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle playlist dal database", e);
        }

        return listaPlaylist;
    }
        
    /**
     * Associa una traccia a una playlist all'interno della tabella playlist_tracks.
     * * @param playlistId ID della playlist
     * @param trackId ID della traccia
     * @return true se l'inserimento ha successo
     * @throws TrackAlreadyInPlaylistException se la traccia è già presente nella playlist
     * @throws RuntimeException in caso di altri errori SQL o di connessione
     */
    @Override
    public boolean addTrackToPlaylist(int playlistId, int trackId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_TRACK_TO_PLAYLIST)) {

            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, trackId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;

        } catch (SQLException e) {
            // "23505" è il codice di stato PostgreSQL nativo per UNIQUE_VIOLATION.
            // Identifica il tentativo di inserimento di un record duplicato nella tabella di giunzione.
            if ("23505".equals(e.getSQLState())) {
                throw new TrackAlreadyInPlaylistException(
                    "La traccia con ID " + trackId + " è già presente nella playlist con ID " + playlistId
                );
            }
            throw new RuntimeException("Errore SQL durante l'aggiunta della traccia alla playlist", e);
        }
    }

    /**
     * Rimuove l'associazione tra una traccia e una playlist.
     * @param playlistId ID della playlist
     * @param trackId ID della traccia da rimuovere
     * @return true se la rimozione ha successo (una riga eliminata), false altrimenti
     */
    @Override
    public boolean removeTrackFromPlaylist(int playlistId, int trackId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(REMOVE_TRACK_FROM_PLAYLIST)) {

            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, trackId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante la rimozione della traccia dalla playlist", e);
        }
    }
    
    /**
     * T-013/01: Estrae tutti i brani associati a una specifica playlist tramite una JOIN.
     * @param playlistId L'ID della playlist da esplorare
     * @return Una lista di oggetti Track ordinati alfabeticamente
     */
    @Override
    public List<Track> getTracksByPlaylist(int playlistId) {
        List<Track> tracks = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TRACKS_BY_PLAYLIST)) {

            pstmt.setInt(1, playlistId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Track track = new Track();
                    track.setId(rs.getInt("id"));
                    track.setTitle(rs.getString("title"));
                    track.setArtist(rs.getString("artist"));
                    track.setLength(rs.getInt("length"));
                    track.setAlbum(rs.getString("album"));
                    track.setPublicationYear(rs.getInt("publication_year"));
                    track.setGenre(rs.getString("genre"));
                    track.setImage(rs.getString("image"));
                    
                    tracks.add(track);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'estrazione delle tracce per la playlist " + playlistId, e);
        }

        return tracks;
    }
}