package com.example.selfworkout.util;

import com.example.selfworkout.model.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Sahne yönetimi için singleton sınıf
 * Farklı ekranlar arasında geçiş yapmayı sağlar
 */
public class SceneManager {

    private static SceneManager instance;
    private User currentUser;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
