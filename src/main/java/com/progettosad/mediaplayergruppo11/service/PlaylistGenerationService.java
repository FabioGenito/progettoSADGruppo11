/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;

/**
 *
 * @author Fabio
 */
public class PlaylistGenerationService {
    private final TrackDAOInterface trackDAO;
    
    public PlaylistGenerationService(TrackDAOInterface trackDAO) {
        this.trackDAO = trackDAO;
    }
    
    public Playlist generatePlaylist(FilterType criteria, Object value, int requestedTracksN) {
        List<Track> extractedTracks = trackDAO.getTracksByCriteria(criteria, value, requestedTracksN);
        
        return assembleTemporaryPlaylist("Mix " + value.toString(), extractedTracks);
    }
    
    /**
     * Crea una playlist con ID fittizio (-1) e vi inserisce i brani.
     */
    private Playlist assembleTemporaryPlaylist(String title, List<Track> tracks) {
        Playlist tempPlaylist = new Playlist(-1, title, "default_auto_playlist.png");        
        tempPlaylist.setTracks(tracks);         
        return tempPlaylist;
    }
    
}
