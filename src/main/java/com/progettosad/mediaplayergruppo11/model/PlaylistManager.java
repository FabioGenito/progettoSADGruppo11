package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.dao.PlaylistDAO;
import com.progettosad.mediaplayergruppo11.exception.TrackAlreadyInPlaylistException;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.observer.Subject;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;

public class PlaylistManager implements Subject {

    // Costante per identificare univocamente l'evento di modifica di una playlist
    public static final String EVENT_TRACK_ADDED_TO_PLAYLIST_PREFIX = "ADDED_TO_PLAYLIST_";
    // Costante per identificare univocamente l'evento di rimozione da una playlist
    public static final String EVENT_TRACK_REMOVED_FROM_PLAYLIST_PREFIX = "REMOVED_FROM_PLAYLIST_";

    private static PlaylistManager instance;
    private String state;
    private PlaylistDAO dao;
    private List<Observer> observers = new ArrayList<>();

    // Costruttore privato per il Singleton
    private PlaylistManager() {
        this.dao = new PlaylistDAO();
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
     * * @param playlistId L'ID della playlist di destinazione
     * @param trackId L'ID della traccia da aggiungere
     */
    public void addTrackToPlaylist(int playlistId, int trackId) {
        try {
            //Delega l'operazione al livello Dati
            boolean isAdded = dao.addTrackToPlaylist(playlistId, trackId);

            // Se ha successo, aggiorna lo stato e notifica l'interfaccia
            if (isAdded) {
                // Lo stato contiene l'ID della playlist modificata, così la UI sa cosa aggiornare
                this.state = EVENT_TRACK_ADDED_TO_PLAYLIST_PREFIX + playlistId;
                System.out.println("PlaylistManager: Traccia " + trackId + " aggiunta alla playlist " + playlistId + ". Emetto notifica...");
                notifyObservers();
            }

        } catch (TrackAlreadyInPlaylistException e) {
            // Intercetta l'eccezione personalizzata per i duplicati
            System.err.println("PlaylistManager Avviso: " + e.getMessage());
            
            // Nota per te che ti occuperai dell'interfaccia: potresti mostrare 
            //un messaggio al Controller affinché mostri un Alert()
            //all'utente ("Brano già presente").

        } catch (RuntimeException e) {
            // Intercetta errori critici
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
        //Creazione del Task asincrono
        Task<Boolean> removeTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Questo codice viene eseguito in un thread separato in background
                return dao.removeTrackFromPlaylist(playlistId, trackId);
            }
        };

        //Comportamento in caso di successo (Eseguito automaticamente sul thread della UI)
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

        //Comportamento in caso di errore
        removeTask.setOnFailed(event -> {
            System.err.println("PlaylistManager [Async] Errore di Sistema: Impossibile rimuovere la traccia.");
            removeTask.getException().printStackTrace();
        });

        //Avvio effettivo del thread
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
}