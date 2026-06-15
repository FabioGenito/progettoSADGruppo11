/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 *
 * @author Fabio
 */

/**
 * Finto DAO in memoria utilizzato esclusivamente per gli Unit Test.
 * Simula il comportamento del database relazionale e funge da Stub per i servizi di raccomandazione.
 */
public class FakeTrackDAO implements TrackDAOInterface {

    private Map<Integer, Track> databaseInMemory = new HashMap<>();
    private int autoIncrementId = 1;
    
    // --- VARIABILI DI CONTROLLO PER I TEST  ---
    public boolean simulateEmptyHistory = false;
    public boolean returnFewerTracks = false;

    // --- METODI ORIGINALI DEL FAKE  ---

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

    // --- NUOVI METODI PER TESTARE IL PLAYLIST GENERATION E LE PREFERENZE ---

    @Override
    public List<Track> getTracksByCriteria(FilterType criteria, Object value, int limit) {
        List<Track> tracks = new ArrayList<>();
        int tracksToReturn = returnFewerTracks ? limit - 2 : limit; 
        for (int i = 0; i < Math.max(1, tracksToReturn); i++) {
            tracks.add(new Track("Track " + i, "Artist", 180, "Album", 2000, value.toString(), ""));
        }
        return tracks;
    }

    @Override
    public List<String> getTopFavoriteGenres(int limit) {
        if (simulateEmptyHistory) return new ArrayList<>(); 
        return Arrays.asList("Rock", "Pop", "Jazz").subList(0, Math.min(limit, 3));
    }

    @Override
    public List<String> getMostFrequentGenres(int limit) {
        return Arrays.asList("Classica", "Rap");
    }

    @Override
    public List<Integer> getTopFavoriteDecades(int limit) {
        if (simulateEmptyHistory) return new ArrayList<>();
        return Arrays.asList(1990, 1980).subList(0, Math.min(limit, 2));
    }

    @Override
    public List<Integer> getMostFrequentDecades(int limit) {
        return Arrays.asList(2010, 2020);
    }

    @Override
    public List<Track> getRandomTracksByGenre(String genre, int limit) {
        return Arrays.asList(new Track("Traccia " + genre, "A", 200, "Al", 2000, genre, ""));
    }

    @Override
    public List<Track> getRandomTracksByDecade(int decade, int limit) {
        return Arrays.asList(new Track("Traccia Anni " + decade, "B", 200, "Al", decade, "Pop", ""));
    }
    
    @Override
    public void incrementPlayCount(int trackId) {
}

    @Override
    public List<Track> getMostPlayedTracksByUser(int userId, int limit) {
        return new ArrayList<>(); // Ritorna una lista vuota per i test
    }
}