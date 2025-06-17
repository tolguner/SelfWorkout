module com.example.selfworkout {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;

    opens com.example.selfworkout to javafx.fxml;
    opens com.example.selfworkout.controller to javafx.fxml;
    opens com.example.selfworkout.model to javafx.base;

    exports com.example.selfworkout;
    exports com.example.selfworkout.controller;
    exports com.example.selfworkout.model;
    exports com.example.selfworkout.util;
    exports com.example.selfworkout.service;
}
