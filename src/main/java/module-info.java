module com.banking {
    requires javafx.controls;
    requires javafx.fxml;
    requires junit;


    opens com.banking to javafx.fxml;
    exports com.banking;
}