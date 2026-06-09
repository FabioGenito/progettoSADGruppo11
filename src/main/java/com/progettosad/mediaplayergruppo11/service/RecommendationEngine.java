/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fabio
 */

public class RecommendationEngine {

    private static RecommendationEngine instance;
    private UserPreferencesService preferencesService;
    private TrackDAOInterface trackDAO;

    // Costruttore privato (Singleton Pattern)
    private RecommendationEngine() {
        this.trackDAO = new TrackDAO();
        this.preferencesService = new UserPreferencesService(this.trackDAO);
    }
    
    protected static void setTestInstance(TrackDAOInterface stubDao) {
        instance = new RecommendationEngine();
        // Richiede di togliere il 'final' alle variabili di istanza, 
        // oppure di re-istanziare i servizi con lo stub:
        instance.trackDAO = stubDao;
        instance.preferencesService = new UserPreferencesService(stubDao);
    }

    public static synchronized RecommendationEngine getInstance() {
        if (instance == null) {
            instance = new RecommendationEngine();
        }
        return instance;
    }

    /**
     * Genera una lista di playlist su misura basate su generi e decenni preferiti.
     * @param tracksPerPlaylist Il numero 'N' di brani da inserire in ogni mix (es. 15).
     * @return Una lista di oggetti Playlist virtuali (ID = -1).
     */
    public List<Playlist> getCustomPlaylists(int tracksPerPlaylist) {
        List<Playlist> customPlaylists = new ArrayList<>();
        
        List<String> topGenres = preferencesService.getRecommendedGenres(3);
        List<Integer> topDecades = preferencesService.getRecommendedDecades(2);

        for (String genre : topGenres) {
            List<Track> tracks = trackDAO.getRandomTracksByGenre(genre, tracksPerPlaylist);
            
            if (!tracks.isEmpty()) {
                Playlist mix = new Playlist(-1, "Il tuo Mix " + genre, "mix_genre_cover.png");
                mix.setTracks(tracks);
                customPlaylists.add(mix);
            }
        }

        for (Integer decade : topDecades) {
            List<Track> tracks = trackDAO.getRandomTracksByDecade(decade, tracksPerPlaylist);
            
            if (!tracks.isEmpty()) {
                Playlist mix = new Playlist(-1, "I tuoi preferiti degli anni '" + String.valueOf(decade).substring(2) + "s", "mix_decade_cover.png");
                mix.setTracks(tracks);
                customPlaylists.add(mix);
            }
        }

        return customPlaylists;
    }
}
