/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.service.PlaylistGenerationService;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.dao.factory.DatabaseDAOFactory;
import com.progettosad.mediaplayergruppo11.model.FilterType;
import com.progettosad.mediaplayergruppo11.model.Playlist;
import com.progettosad.mediaplayergruppo11.model.PlaylistManager;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.view.dialogs.AlertUtils;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
        generationService = new PlaylistGenerationService(DatabaseDAOFactory.getInstance().getTrackDAO());

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
        card.setSpacing(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(200, 280); 
        card.setMinSize(200, 280);
        card.setStyle("-fx-background-color: #181818; -fx-background-radius: 8; -fx-cursor: default; -fx-padding: 15;");

        Region coverImage = new Region();
        coverImage.setPrefSize(160, 160);
        coverImage.setMinSize(160, 160);
        coverImage.setMaxSize(160, 160);
        coverImage.setStyle("-fx-background-color: #333333; -fx-background-radius: 6;");

        Label titleLabel = new Label(tempPlaylist.getName());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        Label infoLabel = new Label(tempPlaylist.getTracks().size() + " brani");
        infoLabel.setTextFill(Color.web("#a7a7a7"));
        infoLabel.setFont(Font.font("System", 12));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); 

        Button btnPlay = new Button("▶ Riproduci");
        btnPlay.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-radius: 20; -fx-text-fill: white; -fx-cursor: hand;");
        btnPlay.setOnAction(e -> {
            Track firstTrack = tempPlaylist.getTracks().get(0);
            PlaybackEngine.getInstance().playSelection(firstTrack, tempPlaylist.getTracks());
        });

        Button btnSave = new Button("Salva");
        btnSave.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> savePlaylistToDatabase(tempPlaylist, btnSave));

        buttonBox.getChildren().addAll(btnPlay, btnSave);

        card.getChildren().addAll(coverImage, titleLabel, infoLabel, buttonBox);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-cursor: default; -fx-padding: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #181818; -fx-background-radius: 8; -fx-cursor: default; -fx-padding: 15;"));

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
