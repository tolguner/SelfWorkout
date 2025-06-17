package com.example.selfworkout.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType; // showConfirmation için eklendi
import java.util.Optional; // showConfirmation için eklendi

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

    // YENİ EKLENDİ: Başarı mesajı göstermek için metod
    public static void showSuccess(String title, String content) {
        // Başarı mesajları genellikle INFO tipi ile gösterilir
        showAlert(AlertType.INFORMATION, title, content);
    }

    // YENİ EKLENDİ: Onay mesajı göstermek için metod
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Onay"); // Standart onay başlığı
        alert.setHeaderText(title); // Parametre olarak gelen başlık
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Başlık altında ek bir başlık göstermez
        alert.setContentText(content);
        alert.showAndWait();
    }
}