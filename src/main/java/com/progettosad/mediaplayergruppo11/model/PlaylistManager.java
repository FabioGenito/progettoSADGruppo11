package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.dao.PlaylistDAOInterface;
import com.progettosad.mediaplayergruppo11.db.DatabaseManager;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.observer.Subject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;

public class PlaylistManager implements Subject {

    public static final String EVENT_TRACK_ADDED_TO_PLAYLIST_PREFIX = "ADDED_TO_PLAYLIST_";
    public static final String EVENT_TRACK_REMOVED_FROM_PLAYLIST_PREFIX = "REMOVED_FROM_PLAYLIST_";

    private static PlaylistManager instance;
    private String state;
    private PlaylistDAOInterface dao;
    private List<Observer> observers = new ArrayList<>();

    private PlaylistManager() {
        this.dao = new PlaylistDAO();
    }

    public void setDao(PlaylistDAOInterface dao) {
        this.dao = dao;
    }
    
    // pattern Singleton
    public static synchronized PlaylistManager getInstance() {
        if (instance == null) {
            instance = new PlaylistManager();
        }
        return instance;
    }

    /**
     * Coordina l'aggiunta di una traccia a una playlist, gestendo le eccezioni
     * e notificando gli Observer in caso di successo.
     * @param playlistId L'ID della playlist di destinazione
     * @param trackId L'ID della traccia da aggiungere
     */
    public void addTrackToPlaylist(int playlistId, int trackId) {
        try {
            boolean isAdded = dao.addTrackToPlaylist(playlistId, trackId);

            if (isAdded) {
                this.state = EVENT_TRACK_ADDED_TO_PLAYLIST_PREFIX + playlistId;
                System.out.println("PlaylistManager: Traccia " + trackId + " aggiunta alla playlist " + playlistId + ". Emetto notifica...");
                notifyObservers();
            }

        } catch (TrackAlreadyInPlaylistException e) {
            System.err.println("PlaylistManager Avviso: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("PlaylistManager Errore di Sistema: Impossibile completare l'operazione.");
            e.printStackTrace();
        }
    }
    
    /**
     * Coordina la rimozione di una traccia in modo asincrono per non bloccare la UI.
     * @param playlistId L'ID della playlist
     * @param trackId L'ID della traccia da scollegare
     */
    public void removeTrackFromPlaylistAsync(int playlistId, int trackId) {
        Task<Boolean> removeTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dao.removeTrackFromPlaylist(playlistId, trackId);
            }
        };

        removeTask.setOnSucceeded(event -> {
            boolean isRemoved = removeTask.getValue();
            if (isRemoved) {
                this.state = EVENT_TRACK_REMOVED_FROM_PLAYLIST_PREFIX + playlistId;
                System.out.println("PlaylistManager [Async]: Traccia " + trackId + " rimossa dalla playlist " + playlistId + ". Emetto notifica...");
                notifyObservers();
            } else {
                System.err.println("PlaylistManager [Async]: Nessuna associazione trovata da rimuovere.");
            }
        });

        removeTask.setOnFailed(event -> {
            System.err.println("PlaylistManager [Async] Errore di Sistema: Impossibile rimuovere la traccia.");
            removeTask.getException().printStackTrace();
        });

        Thread backgroundThread = new Thread(removeTask);
        backgroundThread.setDaemon(true); // Permette all'applicazione di chiudersi anche se il thread è in esecuzione
        backgroundThread.start();
    }
    
    /**
     * Recupera l'elenco completo delle playlist delegando l'operazione al DAO.
     * Gestisce i potenziali errori di sistema restituendo una lista vuota per sicurezza.
     * @return Lista di Playlist o lista vuota in caso di errore.
     */
    public List<Playlist> getAllPlaylists() {
        try {
            return dao.getAllPlaylists();
        } catch (RuntimeException e) {
            System.err.println("PlaylistManager Errore: Impossibile recuperare le playlist.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Recupera le tracce associate a una specifica playlist delegando al DAO.
     * Gestisce i potenziali errori di sistema per proteggere la UI.
     * @param playlistId L'ID della playlist da esplorare
     * @return Una lista di oggetti Track, oppure una lista vuota in caso di errore.
     */
    public List<Track> getTracksByPlaylist(int playlistId) {
        try {
            return dao.getTracksByPlaylist(playlistId);
        } catch (RuntimeException e) {
            System.err.println("PlaylistManager Errore: Impossibile recuperare le tracce per la playlist " + playlistId);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // --- Implementazione dei metodi dell'interfaccia Subject ---

    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    public String getState() {
        return state;
    }
    
    public Playlist createPlaylist(String name, String image) {
        try {
            Playlist newPlaylist = dao.createPlaylist(name, image);
            
            System.out.println("PlaylistManager: Playlist '" + name + "' creata con successo (ID: " + newPlaylist.getId() + ").");
            return newPlaylist;

        } catch (IllegalArgumentException e) {
            System.err.println("PlaylistManager Avviso di Validazione: " + e.getMessage());
            throw e; // Rilancia al Controller in modo che possa mostrare l'errore all'utente
        } catch (RuntimeException e) {
            System.err.println("PlaylistManager Errore di Sistema: Impossibile creare la playlist nel database.");
            e.printStackTrace();
            throw e;
        }
    }
 
    
}