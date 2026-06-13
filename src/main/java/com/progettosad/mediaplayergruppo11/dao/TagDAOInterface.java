/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import com.progettosad.mediaplayergruppo11.model.Tag;
import java.util.List;

/**
 * Interfaccia di astrazione per le operazioni CRUD sull'entità Tag.
 * @author Fabio
 */

public interface TagDAOInterface {
    
    /**
     * Inserisce un nuovo tag nel database previa validazione.
     * @param tag L'oggetto Tag da inserire.
     * @return Il tag inserito comprensivo di ID generato.
     */
    Tag insertTag(Tag tag);

    /**
     * Elimina un tag dal database tramite il suo ID.
     * @param tagId ID del tag da rimuovere.
     * @return true se l'operazione ha successo, false altrimenti.
     */
    boolean deleteTag(int tagId);

    /**
     * Recupera tutti i tag associati a una specifica traccia.
     * @param trackId ID della traccia musicale.
     * @return Lista di tag associati.
     */
    List<Tag> getTagsByTrackId(int trackId);
}
