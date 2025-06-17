package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.ServiceManager;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Tüm Controller sınıfları için temel sınıf
 * Ortak fonksiyonalite ve service erişimi sağlar
 */
public abstract class BaseController implements Initializable {
    
    protected ServiceManager serviceManager;
    protected Stage currentStage;
    protected User currentUser;
    protected Object parentController; // Parent controller referansı
    
    public BaseController() {
        this.serviceManager = ServiceManager.getInstance();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Location kontrolü
        if (location == null) {
            System.err.println("⚠️ BaseController initialize: Location is not set! Controller: " + this.getClass().getSimpleName());
            return;
        }
        
        initializeController();
    }
    
    /**
     * Alt sınıflar tarafından override edilecek initialization metodu
     */
    protected abstract void initializeController();
    
    /**
     * Stage referansını set eder
     */
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }
    
    /**
     * Mevcut kullanıcıyı set eder
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Mevcut kullanıcıyı döndürür
     */
    protected User getCurrentUser() {
        return this.currentUser;
    }
    
    /**
     * Parent controller'ı set eder
     */
    public void setParent(Object parent) {
        this.parentController = parent;
    }
    
    /**
     * Parent controller'ı döndürür
     */
    protected Object getParent() {
        return this.parentController;
    }
    
    /**
     * Bilgi mesajı gösterir
     */
    protected void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Hata mesajı gösterir
     */
    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Uyarı mesajı gösterir
     */
    protected void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyarı");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Onay mesajı gösterir
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Onay");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Başarı mesajı gösterir
     */
    protected void showSuccess(String message) {
        showInfo("Başarılı", message);
    }
    
    /**
     * Loading durumunu gösterir/gizler
     */
    protected void setLoading(boolean loading) {
        if (currentStage != null) {
            if (loading) {
                currentStage.getScene().setCursor(javafx.scene.Cursor.WAIT);
            } else {
                currentStage.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            }
        }
    }
    
    /**
     * Form alanlarını temizler - alt sınıflar override edebilir
     */
    protected void clearForm() {
        // Alt sınıflar implement edecek
    }
    
    /**
     * Form validasyonu - alt sınıflar override edebilir
     */
    protected boolean validateForm() {
        return true; // Alt sınıflar implement edecek
    }
    
    /**
     * Verileri yeniler - alt sınıflar override edebilir
     */
    protected void refreshData() {
        // Alt sınıflar implement edecek
    }
} 
