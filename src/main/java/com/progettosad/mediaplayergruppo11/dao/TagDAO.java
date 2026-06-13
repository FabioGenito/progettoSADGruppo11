/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.db.DatabaseManager;
import com.progettosad.mediaplayergruppo11.model.Tag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fabio
 */

/**
 * Implementazione del DAO per la gestione della persistenza dei Tag.
 * Include logiche di validazione dei dati prima dell'inserimento.
 * * @author gcucc
 */
public class TagDAO implements TagDAOInterface {

    private static final int MAX_TAG_LENGTH = 50;

    // Query SQL estratte in costanti nominate
    private static final String INSERT_TAG = "INSERT INTO tags (name, track_id) VALUES (?, ?)";
    private static final String DELETE_TAG = "DELETE FROM tags WHERE id = ?";
    private static final String SELECT_BY_TRACK = "SELECT * FROM tags WHERE track_id = ? ORDER BY name ASC";
    private static final String CHECK_DUPLICATE = "SELECT COUNT(*) FROM tags WHERE name = ? AND track_id = ?";

    @Override
    public Tag insertTag(Tag tag) {
        validateTag(tag);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TAG, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tag.getName().trim());
            pstmt.setInt(2, tag.getTrackId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tag.setId(generatedKeys.getInt(1));
                    }
                }
            }
            return tag;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'inserimento del tag", e);
        }
    }

    @Override
    public boolean deleteTag(int tagId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TAG)) {

            pstmt.setInt(1, tagId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante l'eliminazione del tag", e);
        }
    }

    @Override
    public List<Tag> getTagsByTrackId(int trackId) {
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TRACK)) {

            pstmt.setInt(1, trackId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Tag tag = new Tag();
                    tag.setId(rs.getInt("id"));
                    tag.setName(rs.getString("name"));
                    tag.setTrackId(rs.getInt("track_id"));
                    tags.add(tag);
                }
            }
            return tags;

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL durante il recupero dei tag della traccia: " + trackId, e);
        }
    }

    /**
     * Metodo helper per centralizzare la logica di validazione del Tag.
     * Modulare e focalizzato su una singola sotto-operazione.
     */
    private void validateTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Il tag non può essere nullo.");
        }
        
        String name = tag.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del tag non può essere vuoto o nullo.");
        }

        if (name.trim().length() > MAX_TAG_LENGTH) {
            throw new IllegalArgumentException("Il tag non può superare i " + MAX_TAG_LENGTH + " caratteri.");
        }

        if (tag.getTrackId() <= 0) {
            throw new IllegalArgumentException("ID traccia associato non valido.");
        }

        if (isDuplicate(name.trim(), tag.getTrackId())) {
            throw new IllegalArgumentException("Il tag '" + name.trim() + "' è già associato a questa traccia.");
        }
    }

    /**
     * Verifica la presenza di duplicati nel database per la stessa traccia.
     */
    private boolean isDuplicate(String name, int trackId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_DUPLICATE)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, trackId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo dei tag duplicati", e);
        }
        return false;
    }
}