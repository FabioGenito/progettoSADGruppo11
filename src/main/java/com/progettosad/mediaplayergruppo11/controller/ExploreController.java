/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.PlaylistGenerationService;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Fabio
 */

public class ExploreController implements Initializable {

    @FXML private ComboBox<FilterType> filterTypeCombo;
    @FXML private TextField filterValueField;
    @FXML private Spinner<Integer> trackLimitSpinner;
    @FXML private TilePane playlistTilePane;

    private PlaylistGenerationService generationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generationService = new PlaylistGenerationService(new TrackDAO());

        filterTypeCombo.getItems().setAll(FilterType.values());
        filterTypeCombo.getSelectionModel().selectFirst();

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10);
        trackLimitSpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void handleGeneratePlaylist() {
        FilterType selectedType = filterTypeCombo.getValue();
        String rawValue = filterValueField.getText();
        int requestedTracks = trackLimitSpinner.getValue();

        if (rawValue == null || rawValue.trim().isEmpty()) {
            AlertUtils.show(Alert.AlertType.WARNING, "Dati mancanti", "Inserisci un valore per il filtro (es. 'Pop' o '2023').");
            return;
        }

        Object filterValue = rawValue.trim();
        
        if (selectedType == FilterType.YEAR) {
            try {
                filterValue = Integer.parseInt((String) filterValue);
            } catch (NumberFormatException e) {
                AlertUtils.show(Alert.AlertType.ERROR, "Formato non valido", "L'anno deve essere un numero valido.");
                return;
            }
        }

        try {
            Playlist generatedPlaylist = generationService.generatePlaylist(selectedType, filterValue, requestedTracks);

            if (generatedPlaylist.getTracks().isEmpty()) {
                AlertUtils.show(Alert.AlertType.WARNING, "Nessun risultato", "Non sono stati trovati brani per il criterio selezionato.");
                return;
            }

            if (generatedPlaylist.getTracks().size() < requestedTracks) {
                AlertUtils.show(Alert.AlertType.INFORMATION, "Brani insufficienti", 
                    "La base dati contiene solo " + generatedPlaylist.getTracks().size() + 
                    " brani per questo criterio, invece dei " + requestedTracks + " richiesti. La playlist è stata generata con i brani disponibili.");
            }

            renderPlaylistCard(generatedPlaylist);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore durante la generazione.");
        }
    }

    /**
     * Crea e aggiunge dinamicamente un VBox rappresentante la Card della Playlist.
     */
    private void renderPlaylistCard(Playlist tempPlaylist) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #181818; -fx-background-radius: 8; -fx-border-color: #282828; -fx-border-radius: 8;");
        card.setPrefWidth(200);

        Label titleLabel = new Label(tempPlaylist.getName());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label infoLabel = new Label(tempPlaylist.getTracks().size() + " brani trovati");
        infoLabel.setTextFill(Color.web("#a7a7a7"));

        Button btnPlay = new Button("▶ Riproduci");
        btnPlay.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-radius: 20; -fx-text-fill: white; -fx-cursor: hand;");
        btnPlay.setMaxWidth(Double.MAX_VALUE);
        
        // Azione Riproduci: delega al motore di playback
        btnPlay.setOnAction(e -> {
            Track firstTrack = tempPlaylist.getTracks().get(0);
            PlaybackEngine.getInstance().playSelection(firstTrack, tempPlaylist.getTracks());
        });

        Button btnSave = new Button("Salva");
        btnSave.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        
        btnSave.setOnAction(e -> savePlaylistToDatabase(tempPlaylist, btnSave));

        card.getChildren().addAll(titleLabel, infoLabel, btnPlay, btnSave);
        
        playlistTilePane.getChildren().add(0, card);
    }

    /**
     * Inserisce la playlist nel DB e associa le tracce.
     */
    private void savePlaylistToDatabase(Playlist tempPlaylist, Button btnSave) {
        try {
            PlaylistManager manager = PlaylistManager.getInstance();
            
            Playlist newSavedPlaylist = manager.createPlaylist(tempPlaylist.getName(), tempPlaylist.getImage());
            
            for (Track track : tempPlaylist.getTracks()) {
                manager.addTrackToPlaylist(newSavedPlaylist.getId(), track.getId());
            }

            btnSave.setText("Salvata ✔");
            btnSave.setDisable(true);
            btnSave.setStyle("-fx-background-color: transparent; -fx-text-fill: #1db954; -fx-border-color: #1db954; -fx-border-radius: 20;");

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore di salvataggio", "Impossibile salvare la playlist nel database.");
        }
    }
}
