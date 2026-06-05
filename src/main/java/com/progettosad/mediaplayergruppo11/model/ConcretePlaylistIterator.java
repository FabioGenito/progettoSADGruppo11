/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

import com.progettosad.mediaplayergruppo11.model.strategy.PlaybackStrategy;
import java.util.List;

/**T - 08/01
 *
 * @author irene
 */
public class ConcretePlaylistIterator implements PlaylistIterator{
     
    private List<Track> queue;
    private int currentIndex;
    
    //Costruttore base dell'iteratore.
    
   public ConcretePlaylistIterator(List<Track> queue, int startIndex){
       this.queue=queue;
       this.currentIndex=startIndex;
   }
   
   @Override
   public boolean hasNext() {
        // Recupera la strategia attuale dal motore
        PlaybackStrategy strategy = PlaybackEngine.getInstance().getPlaybackStrategy();
        
        // Verifica se la strategia prevede un brano successivo valido
        Track nextTrack = strategy.getNextTrack(queue, currentIndex);
        return nextTrack != null;
    }
//T-11/01
    @Override
    public Track next() {
        PlaybackStrategy strategy = PlaybackEngine.getInstance().getPlaybackStrategy();
        
        Track nextTrack = strategy.getNextTrack(queue, currentIndex);
        
        if (nextTrack != null) {
            // Aggiorna l'indice con la posizione originale del nuovo brano
            currentIndex = queue.indexOf(nextTrack); 
        }
        return nextTrack;
    }
   //Getter e setter per permettere la manipolazione della coda a runtime
   public List<Track> getQueue(){
       return queue;
   }
   
   public int getCurrentIndex(){
       return currentIndex;
   }
   
   public void setCurrentIndex(int currentIndex){
       this.currentIndex=currentIndex;
   }
}
