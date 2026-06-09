/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.Track;
import java.util.List;

/**
 *
 * @author Lara
 */

public interface PlaylistDAOInterface {
    Playlist createPlaylist(String name, String image);
    List<Playlist> getAllPlaylists();
    boolean addTrackToPlaylist(int playlistId, int trackId);
    boolean removeTrackFromPlaylist(int playlistId, int trackId);
    List<Track> getTracksByPlaylist(int playlistId);
    void updatePlaylistTrackOrder(int playlistId, List<Track> tracks);
}
