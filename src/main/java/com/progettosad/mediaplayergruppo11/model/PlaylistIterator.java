/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

/** T - 08/01
 *Interfaccia per il pattern Iterator dedicato alla navigazione
 * della coda di riproduzione. 
 * @author irene
 */
public interface PlaylistIterator {
    //Verifica se esiste una traccia successiva nella coda. 
    //restituisce true se c'è un brano successivo, false altrimenti
    boolean hasNext();
    
    //Restituisce la traccia successiva e avanza il cursore.
    Track next();

}

