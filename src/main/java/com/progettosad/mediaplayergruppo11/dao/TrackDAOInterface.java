package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per le operazioni di accesso ai dati 
 * relative ai singoli brani musicali (Track).
 */
public interface TrackDAOInterface {
    Track insertTrack(Track track);
    boolean updateTrack(Track track);
    boolean deleteTrack(int trackId);
    List<Track> getAllTracks();
}