/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import java.util.List;

/**
 *
 * @author Fabio
 */
public class UserPreferencesService {

    private final TrackDAOInterface trackDAO;

    // Dependency Injection: garantisce basso accoppiamento e facilita gli Unit Test
    public UserPreferencesService(TrackDAOInterface trackDAO) {
        this.trackDAO = trackDAO;
    }

    /**
     * Restituisce i generi preferiti dall'utente. 
     * Se l'utente non ha uno storico ascolti (es. nuovo utente),
     * applica la politica di ripiego fornendo i generi più frequenti nel catalogo.
     * * @param limit Il numero massimo di generi da restituire.
     * @return Una lista di generi musicali (String).
     */
    public List<String> getRecommendedGenres(int limit) {
        List<String> favoriteGenres = trackDAO.getTopFavoriteGenres(limit);
        
        if (favoriteGenres.isEmpty()) {
            System.out.println("Nessun ascolto storico rilevato. Attivazione fallback generi.");
            return trackDAO.getMostFrequentGenres(limit);
        }
        
        return favoriteGenres;
    }

    /**
     * Restituisce i decenni preferiti dall'utente.
     * Se l'utente non ha storico, restituisce i decenni più frequenti nel database globale.
     * * @param limit Il numero massimo di decenni da restituire.
     * @return Una lista di decenni (es. 1980, 1990, 2020).
     */
    public List<Integer> getRecommendedDecades(int limit) {
        List<Integer> favoriteDecades = trackDAO.getTopFavoriteDecades(limit);
        
        if (favoriteDecades.isEmpty()) {
            System.out.println("Nessun ascolto storico rilevato. Attivazione fallback decenni.");
            return trackDAO.getMostFrequentDecades(limit);
        }
        
        return favoriteDecades;
    }
}
