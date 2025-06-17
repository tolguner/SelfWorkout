package com.example.selfworkout.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtil {
    public static void showError(String title, String content) {
        showAlert(AlertType.ERROR, title, content);
    }

    public static void showInfo(String title, String content) {
        showAlert(AlertType.INFORMATION, title, content);
    }

    public static void showWarning(String title, String content) {
        showAlert(AlertType.WARNING, title, content);
    }

    private static void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
