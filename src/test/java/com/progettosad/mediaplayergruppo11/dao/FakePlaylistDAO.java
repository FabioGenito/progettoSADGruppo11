/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fabio
 */


/**
 * Finto DAO in memoria per testare isolatamente il PlaylistManager.
 */
public class FakePlaylistDAO implements PlaylistDAOInterface {

    private Map<Integer, Playlist> playlists = new HashMap<>();
    
    private Map<Integer, List<Integer>> playlistTracks = new HashMap<>();
    
    private int autoIncrementId = 1;

    @Override
    public Playlist createPlaylist(String name, String image) {
        Playlist p = new Playlist(autoIncrementId++, name, image);
        playlists.put(p.getId(), p);
        playlistTracks.put(p.getId(), new ArrayList<>());
        return p;
    }

    @Override
    public List<Playlist> getAllPlaylists() {
        return new ArrayList<>(playlists.values());
    }

    @Override
    public boolean addTrackToPlaylist(int playlistId, int trackId) {
        playlistTracks.putIfAbsent(playlistId, new ArrayList<>());
        List<Integer> tracksInThisPlaylist = playlistTracks.get(playlistId);
        
        // Simula il vincolo UNIQUE del database relazionale (codice PostgreSQL 23505)
        if (tracksInThisPlaylist.contains(trackId)) {
            throw new TrackAlreadyInPlaylistException(
                "La traccia con ID " + trackId + " è già presente nella playlist con ID " + playlistId
            );
        }
        
        tracksInThisPlaylist.add(trackId);
        return true;
    }

    @Override
    public boolean removeTrackFromPlaylist(int playlistId, int trackId) {
        if (playlistTracks.containsKey(playlistId)) {
            return playlistTracks.get(playlistId).remove((Integer) trackId);
        }
        return false;
    }

    @Override
    public List<Track> getTracksByPlaylist(int playlistId) {
        List<Track> orderedTracks = new ArrayList<>();
        
        if (playlistTracks.containsKey(playlistId)) {
            List<Integer> trackIds = playlistTracks.get(playlistId);
            // Crea tracce simulate rispettando rigorosamente l'ordine della lista
            for (Integer trackId : trackIds) {
                Track t = new Track();
                t.setId(trackId);
                t.setTitle("Traccia Test " + trackId);
                orderedTracks.add(t);
            }
        }
        return orderedTracks;
    }
    @Override
    public void updatePlaylistTrackOrder(int playlistId, List<Track> tracks) {
        if (playlistTracks.containsKey(playlistId)) {
            List<Integer> newOrderIds = new ArrayList<>();
            for (Track t : tracks) {
                newOrderIds.add(t.getId());
            }
            playlistTracks.put(playlistId, newOrderIds);
        }
    }

    @Override
    public boolean updateTrackPositions(int playlistId, List<Integer> trackIdsInOrder) {
        if (playlistTracks.containsKey(playlistId)) {
            playlistTracks.put(playlistId, new ArrayList<>(trackIdsInOrder));
            return true;
        }
        return false;
    }
}
