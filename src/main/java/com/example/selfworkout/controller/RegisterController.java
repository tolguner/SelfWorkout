package com.example.selfworkout.controller;

import com.example.selfworkout.util.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Register ekranÄ± controller sÄ±nÄ±fÄ±
 * Yeni kullanÄ±cÄ± kaydÄ± iÅŸlemlerini yÃ¶netir
 */
public class RegisterController extends BaseController {
    
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private DatePicker birthdatePicker;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button registerButton;
    @FXML private Hyperlink backToLoginButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    @Override
    protected void initializeController() {
        setupUI();
        setupEventHandlers();
    }
    
    /**
     * UI bileÅŸenlerini ayarlar
     */
    private void setupUI() {
        // Loading indicator'Ä± baÅŸlangÄ±Ã§ta gizle
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        // Status label'Ä± temizle
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        
        // Enter tuÅŸu ile kayÄ±t
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyPressed(this::handleKeyPress);
        }
    }
    
    /**
     * Event handler'larÄ± ayarlar
     */
    private void setupEventHandlers() {
        if (registerButton != null) {
            registerButton.setOnAction(e -> handleRegister());
        }
        if (backToLoginButton != null) {
            backToLoginButton.setOnAction(e -> handleBackToLogin());
        }
    }
    
    /**
     * Klavye olaylarÄ±nÄ± iÅŸler
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleRegister();
        }
    }
    
    /**
     * KayÄ±t iÅŸlemini gerÃ§ekleÅŸtirir
     */
    @FXML
    public void handleRegister() {
        if (!validateRegistrationForm()) {
            return;
        }
        
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // UI'yi loading durumuna getir
        setRegistrationLoading(true);
        
        // Background thread'de kayÄ±t iÅŸlemi
        Task<Boolean> registerTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Sporcu rolÃ¼ ID'si (2)
                return serviceManager.getAuthenticationService().register(
                    username, email, password, firstName, lastName, 2
                );
            }
            
            @Override
            protected void succeeded() {
                setRegistrationLoading(false);
                Boolean success = getValue();
                
                if (success) {
                    handleRegistrationSuccess();
                } else {
                    handleRegistrationFailure("KayÄ±t iÅŸlemi baÅŸarÄ±sÄ±z! KullanÄ±cÄ± adÄ± veya email zaten kullanÄ±lÄ±yor olabilir.");
                }
            }
            
            @Override
            protected void failed() {
                setRegistrationLoading(false);
                Throwable exception = getException();
                handleRegistrationFailure("KayÄ±t sÄ±rasÄ±nda hata oluÅŸtu: " + exception.getMessage());
            }
        };
        
        Thread registerThread = new Thread(registerTask);
        registerThread.setDaemon(true);
        registerThread.start();
    }
    
    /**
     * BaÅŸarÄ±lÄ± kayÄ±t iÅŸlemini yÃ¶netir
     */
    private void handleRegistrationSuccess() {
        if (statusLabel != null) {
            statusLabel.setText("ğŸ‰ KayÄ±t baÅŸarÄ±lÄ±! GiriÅŸ ekranÄ±na yÃ¶nlendiriliyorsunuz...");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
        
        // 2 saniye sonra login ekranÄ±na geÃ§
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                try {
                    SceneManager.getInstance().switchToLogin(currentStage);
                } catch (Exception ex) {
                    showError("Ekran GeÃ§iÅŸ HatasÄ±", "GiriÅŸ ekranÄ±na geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + ex.getMessage());
                }
            })
        );
        timeline.play();
    }
    
    /**
     * BaÅŸarÄ±sÄ±z kayÄ±t iÅŸlemini yÃ¶netir
     */
    private void handleRegistrationFailure(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * GiriÅŸ ekranÄ±na dÃ¶nÃ¼ÅŸ
     */
    @FXML
    public void handleBackToLogin() {
        try {
            SceneManager.getInstance().switchToLogin(currentStage);
        } catch (Exception e) {
            showError("Ekran GeÃ§iÅŸ HatasÄ±", "GiriÅŸ ekranÄ±na geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    /**
     * KayÄ±t form validasyonu
     */
    private boolean validateRegistrationForm() {
        // Null kontrolleri
        if (firstNameField == null || lastNameField == null || usernameField == null || 
            emailField == null || passwordField == null || confirmPasswordField == null || 
            termsCheckBox == null) {
            showError("Sistem HatasÄ±", "Form bileÅŸenleri yÃ¼klenemedi!");
            return false;
        }
        
        // Ad kontrolÃ¼
        if (firstNameField.getText().trim().isEmpty()) {
            showError("Validasyon HatasÄ±", "Ad alanÄ± boÅŸ olamaz!");
            firstNameField.requestFocus();
            return false;
        }
        
        // Soyad kontrolÃ¼
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Validasyon HatasÄ±", "Soyad alanÄ± boÅŸ olamaz!");
            lastNameField.requestFocus();
            return false;
        }
        
        // KullanÄ±cÄ± adÄ± kontrolÃ¼
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Validasyon HatasÄ±", "KullanÄ±cÄ± adÄ± boÅŸ olamaz!");
            usernameField.requestFocus();
            return false;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError("Validasyon HatasÄ±", "KullanÄ±cÄ± adÄ± 3-20 karakter arasÄ± olmalÄ± ve sadece harf, rakam, alt Ã§izgi iÃ§ermelidir!");
            usernameField.requestFocus();
            return false;
        }
        
        // Email kontrolÃ¼
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Validasyon HatasÄ±", "E-posta adresi boÅŸ olamaz!");
            emailField.requestFocus();
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Validasyon HatasÄ±", "GeÃ§erli bir e-posta adresi giriniz!");
            emailField.requestFocus();
            return false;
        }
        
        // Åifre kontrolÃ¼
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showError("Validasyon HatasÄ±", "Åifre boÅŸ olamaz!");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            showError("Validasyon HatasÄ±", "Åifre en az 6 karakter olmalÄ±dÄ±r!");
            passwordField.requestFocus();
            return false;
        }
        
        // Åifre eÅŸleÅŸme kontrolÃ¼
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            showError("Validasyon HatasÄ±", "Åifreler eÅŸleÅŸmiyor!");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // KullanÄ±m ÅŸartlarÄ± kontrolÃ¼
        if (!termsCheckBox.isSelected()) {
            showError("Validasyon HatasÄ±", "KullanÄ±m ÅŸartlarÄ±nÄ± kabul etmelisiniz!");
            return false;
        }
        
        // YaÅŸ kontrolÃ¼ (eÄŸer doÄŸum tarihi girilmiÅŸse)
        if (birthdatePicker != null && birthdatePicker.getValue() != null) {
            LocalDate birthdate = birthdatePicker.getValue();
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birthdate.getYear();
            if (birthdate.plusYears(age).isAfter(now)) {
                age--;
            }
            if (age < 13) {
                showError("Validasyon HatasÄ±", "13 yaÅŸÄ±ndan kÃ¼Ã§Ã¼k kullanÄ±cÄ±lar kayÄ±t olamaz!");
                return false;
            }
            if (age > 120) {
                showError("Validasyon HatasÄ±", "GeÃ§erli bir doÄŸum tarihi giriniz!");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Loading durumunu ayarlar
     */
    private void setRegistrationLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        if (registerButton != null) {
            registerButton.setDisable(loading);
        }
        if (backToLoginButton != null) {
            backToLoginButton.setDisable(loading);
        }
        
        // TÃ¼m input alanlarÄ±nÄ± disable et
        if (firstNameField != null) firstNameField.setDisable(loading);
        if (lastNameField != null) lastNameField.setDisable(loading);
        if (usernameField != null) usernameField.setDisable(loading);
        if (emailField != null) emailField.setDisable(loading);
        if (passwordField != null) passwordField.setDisable(loading);
        if (confirmPasswordField != null) confirmPasswordField.setDisable(loading);
        if (birthdatePicker != null) birthdatePicker.setDisable(loading);
        if (termsCheckBox != null) termsCheckBox.setDisable(loading);
        
        if (loading && statusLabel != null) {
            statusLabel.setText("KayÄ±t iÅŸlemi gerÃ§ekleÅŸtiriliyor...");
            statusLabel.setStyle("-fx-text-fill: blue;");
        }
    }
    
    @Override
    protected void clearForm() {
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (usernameField != null) usernameField.clear();
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (birthdatePicker != null) birthdatePicker.setValue(null);
        if (termsCheckBox != null) termsCheckBox.setSelected(false);
        if (statusLabel != null) statusLabel.setText("");
    }
    
    @Override
    protected boolean validateForm() {
        return validateRegistrationForm();
    }
} 


