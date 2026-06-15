/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao.factory;

import com.progettosad.mediaplayergruppo11.dao.*;

/**
 * Concrete Factory che restituisce le implementazioni reali collegate al Database.
 * Implementata come Singleton per evitare spreco di memoria.
 * @author Fabio
 */

public class DatabaseDAOFactory implements DAOFactory {
    
    private static DatabaseDAOFactory instance;

    private DatabaseDAOFactory() {}

    public static synchronized DatabaseDAOFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseDAOFactory();
        }
        return instance;
    }

    @Override
    public TrackDAOInterface getTrackDAO() {
        return new TrackDAO(); 
    }

    @Override
    public PlaylistDAOInterface getPlaylistDAO() {
        return new PlaylistDAO();
    }

    @Override
    public TagDAOInterface getTagDAO() {
        return new TagDAO();
    }
}