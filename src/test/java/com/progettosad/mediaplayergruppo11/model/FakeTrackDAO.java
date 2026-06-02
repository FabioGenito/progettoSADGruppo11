/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Fabio
 */

/**
 * Finto DAO in memoria utilizzato esclusivamente per gli Unit Test.
 * Simula il comportamento del database relazionale senza connettersi a PostgreSQL.
 */
public class FakeTrackDAO implements TrackDAOInterface {

    private Map<Integer, Track> databaseInMemory = new HashMap<>();
    private int autoIncrementId = 1;

    @Override
    public Track insertTrack(Track track) {
        if (track.getTitle() == null || track.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Titolo vuoto");
        }
        track.setId(autoIncrementId++);
        databaseInMemory.put(track.getId(), track);
        return track;
    }

    @Override
    public boolean updateTrack(Track track) {
        if (track.getId() <= 0) throw new IllegalArgumentException("ID non valido");
        
        if (databaseInMemory.containsKey(track.getId())) {
            databaseInMemory.put(track.getId(), track);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTrack(int trackId) {
        return databaseInMemory.remove(trackId) != null;
    }

    @Override
    public List<Track> getAllTracks() {
        return new ArrayList<>(databaseInMemory.values());
    }
}
