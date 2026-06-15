package com.progettosad.mediaplayergruppo11.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la validazione del Design Pattern Observer.
 * Utilizza la tecnica dei "Dummy Object" per testare la meccanica pura 
 * senza dipendere da altre classi del modello.
 */
class ObserverPatternTest {

    //CREAZIONE DEI DUMMY OBJECTS PER IL TEST

    /**
     * Una semplice implementazione fittizia del Subject.
     * Gestisce la lista degli iscritti e le notifiche.
     */
    class DummySubject implements Subject {
        private List<Observer> observers = new ArrayList<>();

        @Override
        public void attach(Observer o) {
            if (!observers.contains(o)) {
                observers.add(o);
            }
        }

        @Override
        public void detach(Observer o) {
            observers.remove(o);
        }

        @Override
        public void notifyObservers(AppEvent event) {
            for (Observer o : observers) {
                //Passiamo l'evento al metodo update
                o.update(event);
            }
        }
        
        // Metodo di utilità solo per i test per verificare quanti sono iscritti
        public int getObserverCount() {
            return observers.size();
        }
    }

    /**
     * Una semplice implementazione fittizia dell'Observer.
     * Funge da Spy Object registrando se ha ricevuto o meno la notifica.
     */
    class DummyObserver implements Observer {
        public boolean hasBeenNotified = false;
        public int notificationCount = 0;

        @Override
        public void update(AppEvent event) {
            hasBeenNotified = true;
            notificationCount++;
        }
    }

    //TEST EFFETTIVI

    private DummySubject subject;

    @BeforeEach
    void setUp() {
        // Prima di ogni test, creiamo un Subject pulito
        subject = new DummySubject();
    }

    @Test
    @DisplayName("attach() dovrebbe aggiungere correttamente un Observer alla lista")
    void testAttachObserver() {
        DummyObserver observer = new DummyObserver();
        
        subject.attach(observer);
        
        assertEquals(1, subject.getObserverCount(), "Il Subject dovrebbe avere esattamente 1 Observer iscritto.");
    }

    @Test
    @DisplayName("notifyObservers() dovrebbe chiamare update() su tutti gli Observer iscritti")
    void testNotifyObservers() {
        
        DummyObserver spia1 = new DummyObserver();
        DummyObserver spia2 = new DummyObserver();
        subject.attach(spia1);
        subject.attach(spia2);

        //Passiamo null all'evento
        subject.notifyObservers(null);

        assertTrue(spia1.hasBeenNotified, "L'Observer 1 doveva ricevere la notifica.");
        assertTrue(spia2.hasBeenNotified, "L'Observer 2 doveva ricevere la notifica.");
        assertEquals(1, spia1.notificationCount, "L'Observer 1 doveva essere notificato esattamente 1 volta.");
    }

    @Test
    @DisplayName("detach() dovrebbe rimuovere l'Observer, che non riceverà più notifiche")
    void testDetachObserver() {
        
        DummyObserver spia = new DummyObserver();
        subject.attach(spia);
        
        //Rimuoviamo l'Observer e poi lanciamo la notifica
        subject.detach(spia);
        
        //Passiamo null all'evento
        subject.notifyObservers(null);

        assertEquals(0, subject.getObserverCount(), "La lista degli iscritti deve essere vuota.");
        assertFalse(spia.hasBeenNotified, "L'Observer rimosso NON doveva ricevere la notifica.");
    }

    @Test
    @DisplayName("attach() dovrebbe evitare di iscrivere lo stesso Observer due volte (Duplicati)")
    void testAttachPreventsDuplicates() {
       
        DummyObserver spia = new DummyObserver();
        
        //Proviamo ad iscriverlo due volte
        subject.attach(spia);
        subject.attach(spia);
        
        subject.notifyObservers(null);

        assertEquals(1, subject.getObserverCount(), "Il Subject non deve ammettere duplicati.");
        assertEquals(1, spia.notificationCount, "La spia deve ricevere 1 sola notifica, anche se iscritta 2 volte.");
    }
}