module com.mycompany.progettosad {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.mycompany.progettosad to javafx.fxml;
    exports com.mycompany.progettosad;
}
