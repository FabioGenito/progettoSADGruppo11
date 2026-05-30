/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.observer.Subject;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author gcucc
 */
public class TrackManager implements Subject{
    private List<Observer> observers = new ArrayList<>();
    private String subjectState;
    
    private TrackDAO trackDAO;

    public TrackManager() {
        this.trackDAO = new TrackDAO();
    }
    
    @Override
    public void attach(Observer o) {
        //aggiunta dell'observer solo se non è già presente
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
        // Scorre la lista degli observer e chiama il loro metodo update()
        for (Observer o : observers) {
            o.update();
        }
    }

    // Metodo per permettere all'Observer di interrogare lo stato
    public String getState() {
        return subjectState;
    }

    /**
     * Coordina la cancellazione della traccia e la notifica alla UI.
     */
    public void deleteTrack(int trackId) {
        
        //Delega l'operazione al database tramite il DAO
        boolean isDeleted = trackDAO.deleteTrack(trackId);

        //Se l'eliminazione ha avuto successo, aggiorna lo stato e notifica
        if (isDeleted) {
            // Imposta lo stato interno di interesse per gli observer
            this.subjectState = "DELETED_TRACK_" + trackId;
            
            System.out.println("TrackManager: Traccia eliminata dal DB. Emetto notifica agli Observer...");
            
            // Scatena l'aggiornamento automatico della UI
            notifyObservers();
            
        } else {
            System.err.println("TrackManager: Impossibile eliminare la traccia (ID non trovato o errore DB).");
        }
    }
}
