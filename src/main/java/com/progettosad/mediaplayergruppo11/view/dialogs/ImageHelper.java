/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.view.dialogs;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Fabio
 */

/**
 * Classe di utilità per la generazione centralizzata di elementi grafici.
 */
public class ImageHelper {

    private static final String DEFAULT_URL = "https://i.etsystatic.com/6048305/r/il/33b54e/3694359783/il_570xN.3694359783_ldzo.jpg";

    /**
     * Genera un contenitore per immagini con caricamento asincrono,
     * angoli arrotondati e fallback automatico all'immagine di default.
     */
    public static StackPane createImageContainer(String imageUrl, double size, double radius) {
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(size, size);
        imageContainer.setMinSize(size, size);
        imageContainer.setStyle("-fx-background-color: #333333; -fx-background-radius: " + radius + ";");
        
        ImageView coverImageView = new ImageView();
        coverImageView.setFitWidth(size);
        coverImageView.setFitHeight(size);
        
        Rectangle clip = new Rectangle(size, size);
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        coverImageView.setClip(clip);
        
        String finalUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : DEFAULT_URL;
        
        try {
            Image img = new Image(finalUrl, true);
            coverImageView.setImage(img);
        } catch (Exception e) {
            System.out.println("Impossibile caricare l'immagine URL: " + finalUrl);
        }
        
        imageContainer.getChildren().add(coverImageView);
        return imageContainer;
    }
}
