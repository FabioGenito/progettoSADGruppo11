/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;

/**
 *
 * @author Lara e Fabio
 */

public class Track {
    private int id;
    private String title;
    private String artist;
    private int length;
    private String album;
    private int publicationYear;
    private String genre;
    private String image;

    // Costruttore vuoto per gli oggetti che verrano instanziati tramite Refletion
    public Track() {}

    // Costruttore
    public Track(String title, String artist, int length,String album, int publicationYear,String genre,String image) {
        this.title = title;
        this.artist = artist;
        this.length=length;
        this.album=album;
        this.publicationYear = publicationYear;
        this.genre=genre;
        this.image=image;
    }

    // Getter e Setter 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
    
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    /* * Converte i secondi totali del brano nel formato stringa "MM:SS".
    * Utilizzato direttamente dalla UI per l'impaginazione dei metadati.
    */
    public String getFormattedLength() {
        if (this.length < 0) return "00:00";
        int minuti = this.length / 60;
        int secondi = this.length % 60;
        return String.format("%02d:%02d", minuti, secondi);
    }
}
