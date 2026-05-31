/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

/**
 *
 * @author Lara
 */
public class Playlist {
    private int id;
    private String name;
    private String image;
    
    // Costruttore vuoto necessario per la Reflection e i Test
    public Playlist() {}

    public Playlist(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    /**
     * Mostrare il nome della playlist a schermo.
     */
    @Override
    public String toString() {
        return this.name;
    }
    
}
