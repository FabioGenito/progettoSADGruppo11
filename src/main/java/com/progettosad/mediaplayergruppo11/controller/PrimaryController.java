package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.App;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import java.io.IOException;
import javafx.fxml.FXML;

public class PrimaryController implements Observer{
    
      //riferimento all'oggetto ConcreteSubject
    private TrackManager subject;
    
    // Memorizza lo stato che deve essere coerente con quello del subject
    private String observerState; 

    public PrimaryController(TrackManager subject) {
        this.subject = subject;
        //gli oggetti interessati a un particolare Subject devono registrarsi presso di esso.
        this.subject.attach(this); 
    }

    //operazione di aggiornamento 
    @Override
    public void update() {
        // L'Observer interroga il subject per ottenere le informazioni e aggiorna il proprio stato
        this.observerState = subject.getState();
        
        // aggiornare l'interfaccia JavaFX del music player
        System.out.println("Controller UI: Ricevuta notifica. Nuovo stato: " + observerState);
    }
    
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
