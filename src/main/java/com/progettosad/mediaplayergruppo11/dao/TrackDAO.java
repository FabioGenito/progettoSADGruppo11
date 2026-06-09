/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.db.DatabaseManager;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import static com.progettosad.mediaplayergruppo11.model.FilterType.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author gcucc
 */

public class TrackDAO implements TrackDAOInterface {

    private static final String INSERT_TRACK = "INSERT INTO tracks (title, artist, length, album, publication_year, genre, image) VALUES (?,?,?,?,?,?,?)";
    private static final String DELETE_TRACK = "DELETE FROM tracks WHERE id = ?";
    private static final String INCREMENT_PLAY_COUNT = "UPDATE tracks SET play_count = play_count + 1 WHERE id = ?";
    private static final String UPDATE_TRACK = "UPDATE tracks SET title = ?, artist = ?, length = ?, album = ?, publication_year = ?, genre = ?, image = ? WHERE id = ?";
    private static final String SELECT_ALL_TRACKS = "SELECT * FROM Tracks ORDER BY title ASC";
    private static final String SELECT_BY_GENRE = "SELECT * FROM tracks WHERE genre = ? ORDER BY RANDOM() LIMIT ?";
    private static final String SELECT_BY_YEAR = "SELECT * FROM tracks WHERE publication_year = ? ORDER BY RANDOM() LIMIT ?";
    private static final String QRY_TOP_GENRES = "SELECT genre, SUM(play_count) FROM tracks GROUP BY genre HAVING SUM(play_count) > 0 ORDER BY 2 DESC LIMIT ?";
    private static final String QRY_TOP_DECADES = "SELECT ((publication_year / 10) * 10) AS decade, SUM(play_count) FROM tracks GROUP BY 1 HAVING SUM(play_count) > 0 ORDER BY 2 DESC LIMIT ?";    
    private static final String QRY_FREQ_GENRES = "SELECT genre, COUNT(id) FROM tracks GROUP BY genre ORDER BY 2 DESC LIMIT ?";
    private static final String QRY_FREQ_DECADES = "SELECT ((publication_year / 10) * 10) AS decade, COUNT(id) FROM tracks GROUP BY 1 ORDER BY 2 DESC LIMIT ?";
    private static final String SELECT_RANDOM_BY_GENRE = "SELECT * FROM tracks WHERE genre = ? ORDER BY RANDOM() LIMIT ?";
    private static final String SELECT_RANDOM_BY_DECADE = "SELECT * FROM tracks WHERE ((publication_year / 10) * 10) = ? ORDER BY RANDOM() LIMIT ?";
    
    @Override
    public List<String> getTopFavoriteGenres(int limit) {
        return fetchStringList(QRY_TOP_GENRES, limit);
    }

    @Override
    public List<Integer> getTopFavoriteDecades(int limit) {
        return fetchIntegerList(QRY_TOP_DECADES, limit);
    }

    @Override
    public List<String> getMostFrequentGenres(int limit) {
        return fetchStringList(QRY_FREQ_GENRES, limit);
    }

    @Override
    public List<Integer> getMostFrequentDecades(int limit) {
        return fetchIntegerList(QRY_FREQ_DECADES, limit);
    }
    
    @Override
    public List<Track> getRandomTracksByGenre(String genre, int limit) {
        return fetchRandomTracks(SELECT_RANDOM_BY_GENRE, genre, limit);
    }

    @Override
    public List<Track> getRandomTracksByDecade(int decade, int limit) {
        return fetchRandomTracks(SELECT_RANDOM_BY_DECADE, decade, limit);
    }
    

    private List<String> fetchStringList(String query, int limit) {
        List<String> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'estrazione delle preferenze (String)", e);
        }
        return results;
    }
    
    private List<Track> fetchRandomTracks(String query, Object filterParam, int limit) {
        List<Track> trackList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setObject(1, filterParam);
            pstmt.setInt(2, limit);
            
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
                    trackList.add(track);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'estrazione mirata delle tracce", e);
        }
        return trackList;
    }
    

    private List<Integer> fetchIntegerList(String query, int limit) {
        List<Integer> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'estrazione delle preferenze (Integer)", e);
        }
        return results;
    }
    
    /**
     * Inserisce un oggetto Track nel database PostgreSQL.
     * * @param track L'oggetto Track da inserire
     * @return L'oggetto Track aggiornato con l'ID generato dal database
     * @throws IllegalArgumentException se il track è nullo o se titolo/autore sono vuoti o nulli
     */
    @Override
    public Track insertTrack(Track track) {
        if (track == null || 
            track.getTitle() == null || track.getTitle().trim().isEmpty() || 
            track.getArtist() == null || track.getArtist().trim().isEmpty()) {
            
            throw new IllegalArgumentException("Il titolo e l'autore non possono essere vuoti o nulli.");
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRACK, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, track.getTitle());
            pstmt.setString(2, track.getArtist());
            pstmt.setInt(3, track.getLength());
            pstmt.setString(4, track.getAlbum());
            pstmt.setInt(5, track.getPublicationYear());
            pstmt.setString(6, track.getGenre());
            pstmt.setString(7, track.getImage());
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        track.setId(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL durante l'inserimento della traccia: " + e.getMessage());
            throw new RuntimeException("Errore di persistenza durante l'inserimento del Track", e);
        }

        return track;
    }

    /**
     * Elimina una traccia dal database utilizzando una transazione esplicita.
     * Notifica gli observer in caso di successo.
     * * @param trackId L'ID della traccia da eliminare
     * @return true se la traccia è stata eliminata, false se non è stata trovata
     */
    @Override
    public boolean deleteTrack(int trackId) {
        boolean success = false;

        try (Connection conn = DatabaseManager.getConnection()) {
            // Disabilitazione autocommit per gestire esplicitamente la transazione atomica
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(DELETE_TRACK)) {
                pstmt.setInt(1, trackId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    success = true;
                } else {
                    conn.rollback();
                    System.out.println("Nessuna traccia trovata con ID: " + trackId);
                }

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Errore SQL durante l'eliminazione. Transazione annullata tramite rollback.", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database durante la cancellazione", e);
        }

        return success;
    }
    
    //incremento del contatore delle riproduzione di un brano 
    public void incrementPlayCount(int trackId){
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INCREMENT_PLAY_COUNT)) {
            pstmt.setInt(1, trackId);
            pstmt.executeUpdate();
            System.out.println("Contatore ascolti aggiornato per la traccia: " + trackId);
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento ascolti: " + e.getMessage());
        }
    }
    
    /**
     * Aggiorna i dati di una traccia esistente nel database PostgreSQL.
     * * @param track L'oggetto Track contenente i dati aggiornati e l'ID da modificare
     * @return true se esattamente un record è stato aggiornato, false altrimenti
     * @throws IllegalArgumentException se l'oggetto è nullo o manca di campi obbligatori (incluso l'ID)
     * @throws RuntimeException in caso di errori di connessione o SQL
     */
    @Override
    public boolean updateTrack(Track track) {
        if (track == null || 
            track.getId() <= 0 || 
            track.getTitle() == null || track.getTitle().trim().isEmpty() || 
            track.getArtist() == null || track.getArtist().trim().isEmpty()) {
            
            throw new IllegalArgumentException("Dati non validi: ID, titolo e artista sono obbligatori per l'aggiornamento.");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TRACK)) {

            pstmt.setString(1, track.getTitle());
            pstmt.setString(2, track.getArtist());
            pstmt.setInt(3, track.getLength());
            pstmt.setString(4, track.getAlbum());
            pstmt.setInt(5, track.getPublicationYear());
            pstmt.setString(6, track.getGenre());
            pstmt.setString(7, track.getImage());
            pstmt.setInt(8, track.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'aggiornamento della traccia con ID " + track.getId(), e);
        }
    }
    
    /**
     * Estrae l'elenco completo delle tracce presenti nel database, ordinate per titolo.
     * * @return Una lista di oggetti Track ordinati alfabeticamente per titolo.
     * Se non ci sono tracce, restituisce una lista vuota.
     * @throws RuntimeException in caso di errori SQL o di connessione.
     */
    @Override
    public List<Track> getAllTracks() {
        List<Track> trackList = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_TRACKS);
             ResultSet rs = pstmt.executeQuery()) {

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

                trackList.add(track);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante il recupero della lista delle tracce.", e);
        }

        return trackList;
    }
    
    @Override
    public List<Track> getTracksByCriteria(FilterType criteria, Object value, int limit) {
        List<Track> trackList = new ArrayList<>();
        String query = null;
        if (criteria == GENRE) query = SELECT_BY_GENRE;
        else if(criteria == YEAR) query = SELECT_BY_YEAR;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, value); 
            pstmt.setInt(2, limit);

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
                    trackList.add(track);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante la generazione automatica della playlist.", e);
        }
        return trackList;
    }
    
}
