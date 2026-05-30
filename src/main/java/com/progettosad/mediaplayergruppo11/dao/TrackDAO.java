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
        
        //Validazione dei dati
        if (track == null || 
            track.getTitle() == null || track.getTitle().trim().isEmpty() || 
            track.getArtist() == null || track.getArtist().trim().isEmpty()) {
            
            throw new IllegalArgumentException("Il titolo e l'autore non possono essere vuoti o nulli.");
        }
        
        String sql = "INSERT INTO tracks (title, artist,length, album, publication_year, genre, image) VALUES (?,?,?,?,?,?,?)";

        //Gestione della connessione e dello statement con try-with-resources
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

            //Recupero dell'ID generato 
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
    public boolean deleteTrack(int trackId) {
        String sql = "DELETE FROM tracks WHERE id = ?";
        boolean success = false;

        // Otteniamo la connessione nel blocco try-with-resources principale
        try (Connection conn = DatabaseManager.getConnection()) {
            
            //Disabilitiamo l'autocommit per iniziare la transazione manuale
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, trackId);
                
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0){
                    conn.commit();
                    success = true;
                } else {
                    conn.rollback();
                    System.out.println("Nessuna traccia trovata con ID: " + trackId);
                }

            } catch (SQLException e) {
                //Errore: effettuiamo il rollback per annullare qualsiasi modifica parziale
                conn.rollback();
                throw new RuntimeException("Errore SQL durante l'eliminazione. Transazione annullata tramite rollback.", e);
            } finally {
                //Ripristiniamo l'autocommit allo stato originale 
                //prima che la connessione venga chiusa o restituita al pool di connessioni
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database durante la cancellazione", e);
        }

        return success;
    }
    //incremento del contatore delle riproduzione di un brano 
    public void incrementPlayCount(int trackId){
        String sql = "UPDATE tracks SET play_count=play_count + 1 WHERE id =?";
        try(Connection conn=DatabaseManager.getConnection();
                PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setInt(1,trackId);
            pstmt.executeUpdate();
            System.out.println("Contatore ascolti aggiornato per la traccia: " + trackId);
        }catch (SQLException e){
            System.err.println("Errore nell'aggiornamento ascolti: " + e.getMessage());
        }
    }
}
