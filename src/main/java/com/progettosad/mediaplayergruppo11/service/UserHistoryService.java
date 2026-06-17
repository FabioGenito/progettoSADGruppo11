package com.progettosad.mediaplayergruppo11.service;

import com.progettosad.mediaplayergruppo11.dao.PlaylistDAOInterface;
import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.Collections;
import java.util.List;

/**T 16/02
 * Servizio che fa da tramite tra i DAO e il Controller
 * per recuperare lo storico degli ascolti personali dell'utente.
 *
 * Segue lo stesso pattern DI (Dependency Injection) di UserPreferencesService:
 * basso accoppiamento, facile da testare con fake DAO.
 */
public class UserHistoryService {

    private final TrackDAOInterface trackDAO;
    private final PlaylistDAOInterface playlistDAO;

    /**
     * Costruttore con Dependency Injection.
     *
     * @param trackDAO    implementazione del DAO per i brani
     * @param playlistDAO implementazione del DAO per le playlist
     */
    public UserHistoryService(TrackDAOInterface trackDAO, PlaylistDAOInterface playlistDAO) {
        this.trackDAO    = trackDAO;
        this.playlistDAO = playlistDAO;
    }

    /**T 16/01 T-16/02
     * Restituisce i brani più ascoltati dall'utente indicato.
     *
     * Gestione "Nuovo Utente": se il DAO restituisce una lista vuota
     * (nessun ascolto registrato) il metodo ritorna una lista vuota
     * senza sollevare eccezioni, lasciando al controller il compito
     * di mostrare il messaggio placeholder appropriato.
     *
     * @param userId l'ID dell'utente corrente
     * @param limit  numero massimo di brani
     * @return lista di Track (mai null)
     */
    public List<Track> getMostPlayedTracks(int userId, int limit) {
        try {
            List<Track> tracks = trackDAO.getMostPlayedTracksByUser(userId, limit);
            return (tracks != null) ? tracks : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("UserHistoryService: errore recupero brani – " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Restituisce le playlist più ascoltate dall'utente indicato.
     *
     * Stessa politica di gestione errori di getMostPlayedTracks.
     *
     * @param userId l'ID dell'utente corrente
     * @param limit  numero massimo di playlist
     * @return lista di Playlist (mai null)
     */
    public List<Playlist> getMostPlayedPlaylists(int userId, int limit) {
        try {
            List<Playlist> playlists = playlistDAO.getMostPlayedPlaylistsByUser(userId, limit);
            return (playlists != null) ? playlists : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("UserHistoryService: errore recupero playlist – " + e.getMessage());
            return Collections.emptyList();
        }
    }
}