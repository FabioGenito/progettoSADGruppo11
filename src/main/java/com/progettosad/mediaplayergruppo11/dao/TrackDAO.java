/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

/**
 *
 * @author gcucc
 */
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TrackDAO {

    /**
     * Inserisce un oggetto Track nel database PostgreSQL.
     * * @param track L'oggetto Track da inserire
     * @return L'oggetto Track aggiornato con l'ID generato dal database
     * @throws IllegalArgumentException se il track è nullo o se titolo/autore sono vuoti o nulli
     */
    public Track insertTrack(Track track) {
        
        // 1. Validazione dei dati
        if (track == null || 
            track.getTitle() == null || track.getTitle().trim().isEmpty() || 
            track.getArtist() == null || track.getArtist().trim().isEmpty()) {
            
            throw new IllegalArgumentException("Il titolo e l'autore non possono essere vuoti o nulli.");
        }

        // 2. Query SQL
        String sql = "INSERT INTO tracks (title, artist,length, album, publication_year, genre, image) VALUES (?,?,?,?,?,?,?)";

        // 3. Gestione della connessione e dello statement con try-with-resources
        //Statement.RETURN_GENERATED_KEYS recupera l'ID autogenerato da PostgreSQL
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Impostazione dei parametri
            pstmt.setString(1, track.getTitle());
            pstmt.setString(2, track.getArtist());
            pstmt.setInt(3, track.getLength());
            pstmt.setString(4, track.getAlbum());
            pstmt.setInt(5, track.getPublicationYear());
            pstmt.setString(6, track.getGenre());
            pstmt.setString(7, track.getImage());
            
            // Esecuzione della query
            int affectedRows = pstmt.executeUpdate();

            // 4. Recupero dell'ID generato 
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
}