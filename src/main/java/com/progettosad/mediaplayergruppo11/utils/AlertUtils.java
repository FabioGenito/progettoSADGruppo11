/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author Fabio
 */

/**
 * Utility per la generazione centralizzata delle finestre di dialogo (Alert) di JavaFX.
 */
public final class AlertUtils {

    private AlertUtils() {
        throw new UnsupportedOperationException("Classe di utilità non istanziabile");
    }

    /**
     * Mostra un Alert sul thread grafico principale in modo sicuro.
     */
    public static void show(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}