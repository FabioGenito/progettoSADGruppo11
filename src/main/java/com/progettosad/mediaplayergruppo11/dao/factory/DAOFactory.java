/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao.factory;

import com.progettosad.mediaplayergruppo11.dao.PlaylistDAOInterface;
import com.progettosad.mediaplayergruppo11.dao.TagDAOInterface;
import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;

/**
 * Abstract Factory per la creazione dei Data Access Object.
 * @author Fabio
 */

public interface DAOFactory {
    TrackDAOInterface getTrackDAO();
    PlaylistDAOInterface getPlaylistDAO();
    TagDAOInterface getTagDAO();
}