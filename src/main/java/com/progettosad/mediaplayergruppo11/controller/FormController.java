package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.dao.TagDAO;
import com.progettosad.mediaplayergruppo11.dao.TagDAOInterface;
import com.progettosad.mediaplayergruppo11.dao.factory.DatabaseDAOFactory;
import com.progettosad.mediaplayergruppo11.model.Tag;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.view.dialogs.AlertUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller per la gestione del form della traccia (Inserimento e Modifica).
 * Refactoring completato: rimosso l'uso di stili grafici hardcoded in Java
 * delegando interamente la formattazione visiva al file CSS esterno.
 * * @author Fabio
 */
public class FormController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField albumField;
    @FXML private TextField minuteField;
    @FXML private TextField secondField;
    @FXML private TextField yearField;
    @FXML private TextField genreField;
    @FXML private TextField imageField;
    
    @FXML private VBox tagSectionContainer;
    @FXML private FlowPane tagsFlowPane;
    @FXML private TextField newTagField;
    @FXML private Button addTagButton;
    
    @FXML private Button submitButton;
    @FXML private Button backButton;
    
    // Costante per identificare la classe di errore CSS (DoD - No magic strings)
    private static final String STYLE_CLASS_ERROR = "text-field-error";
    
    private Track trackToEdit = null;
    private final TagDAOInterface tagDAO = DatabaseDAOFactory.getInstance().getTagDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) { 
        tagSectionContainer.setVisible(false);
        newTagField.textProperty().addListener((obs, oldV, newV) -> resetFieldStyle(newTagField));
        setupGraphicalValidation();
    }
    
    public void setTrackData(Track track) {
        this.trackToEdit = track;        
        submitButton.setText("Salva Modifiche");

        titleField.setText(track.getTitle());
        artistField.setText(track.getArtist());
        albumField.setText(track.getAlbum() != null ? track.getAlbum() : "");
        
        int minuti = track.getLength() / 60;
        int secondi = track.getLength() % 60;
        minuteField.setText(String.valueOf(minuti));
        secondField.setText(String.format("%02d", secondi));
        
        yearField.setText(String.valueOf(track.getPublicationYear()));
        genreField.setText(track.getGenre());
        imageField.setText(track.getImage() != null ? track.getImage() : "");
        
        tagSectionContainer.setVisible(true);
        loadTagsAsync();
        
        validateFields();
    }

    private void loadTagsAsync() {
        if (trackToEdit == null) return;

        Task<List<Tag>> loadTask = new Task<>() {
            @Override
            protected List<Tag> call() throws Exception {
                return tagDAO.getTagsByTrackId(trackToEdit.getId());
            }
        };

        loadTask.setOnSucceeded(e -> populateTagsFlowPane(loadTask.getValue()));
        loadTask.setOnFailed(e -> AlertUtils.show(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare i tag del brano."));

        new Thread(loadTask).start();
    }

    private void populateTagsFlowPane(List<Tag> tags) {
        tagsFlowPane.getChildren().clear();
        for (Tag tag : tags) {
            tagsFlowPane.getChildren().add(createTagChip(tag));
        }
    }

    /**
     * Crea programmaticamente la Chip agganciando le classi definite nel CSS esterno.
     */
    private HBox createTagChip(Tag tag) {
        HBox chip = new HBox();
        chip.getStyleClass().add("tag-chip");

        Label label = new Label(tag.getName());
        label.getStyleClass().add("tag-chip-label");

        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("tag-chip-remove-btn");
        removeBtn.setOnAction(e -> handleRemoveTag(tag, chip));

        chip.getChildren().addAll(label, removeBtn);
        return chip;
    }

    @FXML
    private void handleAddTag() {
        String tagText = newTagField.getText().trim();

        if (tagText.isEmpty()) {
            // Se non è già presente, aggiungiamo la classe d'errore CSS per fare il bordo rosso
            if (!newTagField.getStyleClass().contains(STYLE_CLASS_ERROR)) {
                newTagField.getStyleClass().add(STYLE_CLASS_ERROR);
            }
            return;
        }

        Task<Tag> addSpecTask = new Task<>() {
            @Override
            protected Tag call() throws Exception {
                Tag tag = new Tag();
                tag.setName(tagText);
                tag.setTrackId(trackToEdit.getId());
                return tagDAO.insertTag(tag);
            }
        };

        addSpecTask.setOnSucceeded(e -> {
            newTagField.clear();
            resetFieldStyle(newTagField);
            tagsFlowPane.getChildren().add(createTagChip(addSpecTask.getValue()));
        });

        addSpecTask.setOnFailed(e -> {
            Throwable error = addSpecTask.getException();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore di Validazione", error.getMessage());
        });

        new Thread(addSpecTask).start();
    }

    private void handleRemoveTag(Tag tag, HBox chipNode) {
        Task<Boolean> deleteSpecTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return tagDAO.deleteTag(tag.getId());
            }
        };

        deleteSpecTask.setOnSucceeded(e -> {
            if (deleteSpecTask.getValue()) {
                tagsFlowPane.getChildren().remove(chipNode);
            }
        });

        deleteSpecTask.setOnFailed(e -> AlertUtils.show(Alert.AlertType.ERROR, "Errore", "Impossibile rimuovere il tag selezionato."));

        new Thread(deleteSpecTask).start();
    }

    /**
     * Ripristina lo stile originario rimuovendo la classe di errore CSS.
     */
    private void resetFieldStyle(TextField field) {
        field.getStyleClass().remove(STYLE_CLASS_ERROR);
    }

    private void setupGraphicalValidation() {
        titleField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        artistField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        minuteField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        secondField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        yearField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        genreField.textProperty().addListener((obs, oldV, newV) -> validateFields());
    }

    private void validateFields() {
        boolean isInvalid = titleField.getText().trim().isEmpty() ||
                            artistField.getText().trim().isEmpty() ||
                            minuteField.getText().trim().isEmpty() ||
                            secondField.getText().trim().isEmpty() ||
                            yearField.getText().trim().isEmpty() ||
                            genreField.getText().trim().isEmpty();
                            
        submitButton.setDisable(isInvalid);
    }

    @FXML
    private void handleSaveTrack() {
        Task<Track> saveTask = new Task<Track>() {
            @Override
            protected Track call() throws Exception {
                int minutes, seconds, year;
                try {
                    minutes = Integer.parseInt(minuteField.getText().trim());
                    seconds = Integer.parseInt(secondField.getText().trim());
                    year = Integer.parseInt(yearField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new Exception("I campi Durata (Min/Sec) e Anno devono contenere solo numeri interi.");
                }

                int totalSeconds = (minutes * 60) + seconds;
                
                if(trackToEdit == null) {
                    Track newTrack = new Track(
                        titleField.getText().trim(),
                        artistField.getText().trim(),
                        totalSeconds,
                        albumField.getText().trim(),
                        year,
                        genreField.getText().trim(),
                        imageField.getText().trim()                            
                    );
                    return TrackManager.getInstance().insertNewTrack(newTrack);
                } else {
                    trackToEdit.setTitle(titleField.getText().trim());
                    trackToEdit.setArtist(artistField.getText().trim());
                    trackToEdit.setLength(totalSeconds);
                    trackToEdit.setAlbum(albumField.getText().trim());
                    trackToEdit.setPublicationYear(year);
                    trackToEdit.setGenre(genreField.getText().trim());
                    trackToEdit.setImage(imageField.getText().trim());
                    return TrackManager.getInstance().updateTrack(trackToEdit);
                }
            }
        };

        saveTask.setOnFailed(event -> {
            Throwable error = saveTask.getException();
            String operazione = trackToEdit == null ? "Inserimento" : "Modifica";
            AlertUtils.show(Alert.AlertType.ERROR, "Errore di " + operazione, error.getMessage());
        });

        saveTask.setOnSucceeded(event -> {
            Track processedTrack = saveTask.getValue();
            if (trackToEdit == null) {
                TrackManager.getInstance().notifyTrackAdded(processedTrack);
                AlertUtils.show(Alert.AlertType.INFORMATION, "Successo", "Traccia inserita correttamente!");
            } else {
                TrackManager.getInstance().notifyTrackUpdated(processedTrack);
                AlertUtils.show(Alert.AlertType.INFORMATION, "Successo", "Traccia aggiornata correttamente!");
            }
            navigateToHome();
        });

        new Thread(saveTask).start();
    }
    
    @FXML
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/MainShellView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}