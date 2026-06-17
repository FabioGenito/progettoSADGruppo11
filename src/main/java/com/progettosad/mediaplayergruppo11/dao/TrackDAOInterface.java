package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.FilterType;
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
    List<Track> getTracksByCriteria(FilterType criteria, Object value, int limit);
    List<String> getTopFavoriteGenres(int limit);
    List<Integer> getTopFavoriteDecades(int limit);
    List<String> getMostFrequentGenres(int limit);
    List<Integer> getMostFrequentDecades(int limit);
    List<Track> getRandomTracksByGenre(String genre, int limit);
    List<Track> getRandomTracksByDecade(int decade, int limit);
    List<Track> getMostPlayedTracksByUser(int userId, int limit);
    void incrementPlayCount(int trackId);
}