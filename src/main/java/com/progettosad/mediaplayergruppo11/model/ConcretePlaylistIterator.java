/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

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
   public boolean hasNext(){
       return queue != null && currentIndex +1 < queue.size();
   }
   
   @Override
   public Track next(){
       if (hasNext()){
           currentIndex++;
           return queue.get(currentIndex);
       }
       return null;
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
