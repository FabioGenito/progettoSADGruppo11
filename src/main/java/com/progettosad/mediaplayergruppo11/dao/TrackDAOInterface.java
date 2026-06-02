package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;

/**
 *
 * @author Lara
 */


public interface TrackDAOInterface {
    Track insertTrack(Track track);
    boolean updateTrack(Track track);
    boolean deleteTrack(int trackId);
    List<Track> getAllTracks();
}