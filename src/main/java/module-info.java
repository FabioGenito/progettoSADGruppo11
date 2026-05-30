module com.progettosad.mediaplayergruppo11 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    opens com.progettosad.mediaplayergruppo11 to javafx.fxml;
    opens com.progettosad.mediaplayergruppo11.controller to javafx.fxml;
    opens com.progettosad.mediaplayergruppo11.model to javafx.base;
    exports com.progettosad.mediaplayergruppo11;
}
