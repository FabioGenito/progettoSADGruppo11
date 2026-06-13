/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finto DAO in memoria per l'entità Tag, utilizzato esclusivamente per gli Unit Test.
 * Simula le validazioni di backend e le costrizioni del database (es. unicità).
 * * @author Fabio
 */
public class FakeTagDAO implements TagDAOInterface {

    private final Map<Integer, Tag> databaseInMemory = new HashMap<>();
    private int autoIncrementId = 1;
    private static final int MAX_TAG_LENGTH = 50;

    @Override
    public Tag insertTag(Tag tag) {
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

        // Simula il vincolo UNIQUE(name, track_id) del database
        for (Tag existingTag : databaseInMemory.values()) {
            if (existingTag.getTrackId() == tag.getTrackId() && 
                existingTag.getName().equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException("Il tag '" + name.trim() + "' è già associato a questa traccia.");
            }
        }

        // Assegnazione ID incrementale e salvataggio in memoria
        tag.setId(autoIncrementId++);
        databaseInMemory.put(tag.getId(), new Tag(tag.getId(), tag.getName().trim(), tag.getTrackId()));
        return tag;
    }

    @Override
    public boolean deleteTag(int tagId) {
        return databaseInMemory.remove(tagId) != null;
    }

    @Override
    public List<Tag> getTagsByTrackId(int trackId) {
        List<Tag> results = new ArrayList<>();
        for (Tag tag : databaseInMemory.values()) {
            if (tag.getTrackId() == trackId) {
                results.add(tag);
            }
        }
        // Simula l'ORDER BY name ASC della query SQL originale
        results.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));
        return results;
    }
}