/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.observer;

/**
 *
 * @author gcucc
 */
public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers(AppEvent event);
}
