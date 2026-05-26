module com.progettosad.mediaplayergruppo11 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.progettosad.mediaplayergruppo11 to javafx.fxml;
    exports com.progettosad.mediaplayergruppo11;
}
