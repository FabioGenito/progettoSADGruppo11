/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.view.dialogs;

import javafx.scene.control.*;
import java.util.Optional;
import javafx.util.Pair;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Fabio
 */

public class PlaylistDialog {

    public static Optional<Pair<String, String>> showNewPlaylistDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nuova Playlist");
        dialog.setHeaderText("Inserisci i dettagli della nuova Playlist");

        ButtonType createButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Es: Rock anni 90");
        TextField imageField = new TextField();
        imageField.setPromptText("URL o percorso immagine");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Nome Playlist:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Percorso immagine:"), 0, 1);
        grid.add(imageField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        okButton.disableProperty().bind(nameField.textProperty().isEmpty());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(nameField.getText(), imageField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
