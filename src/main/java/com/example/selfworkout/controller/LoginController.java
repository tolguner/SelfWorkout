package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.util.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.prefs.Preferences;

/**
 * Login ekranÄ± controller sÄ±nÄ±fÄ±
 * KullanÄ±cÄ± giriÅŸi ve kayÄ±t iÅŸlemlerini yÃ¶netir
 */
public class LoginController extends BaseController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    @Override
    protected void initializeController() {
        setupUI();
        setupEventHandlers();
        loadRememberedCredentials();
    }
    
    /**
     * UI bileÅŸenlerini ayarlar
     */
    private void setupUI() {
        // Loading indicator'Ä± baÅŸlangÄ±Ã§ta gizle
        loadingIndicator.setVisible(false);
        
        // Status label'Ä± temizle
        statusLabel.setText("");
        
        // Enter tuÅŸu ile login
        passwordField.setOnKeyPressed(this::handleKeyPress);
        usernameField.setOnKeyPressed(this::handleKeyPress);
    }
    
    /**
     * Event handler'larÄ± ayarlar
     */
    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
        forgotPasswordLink.setOnAction(e -> handleForgotPassword());
    }
    
    /**
     * Klavye olaylarÄ±nÄ± iÅŸler
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    /**
     * Login iÅŸlemini gerÃ§ekleÅŸtirir
     */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Form validasyonu
        if (!validateLoginForm()) {
            return;
        }
        
        // Loading durumunu gÃ¶ster
        setLoginLoading(true);
        
        // GerÃ§ek authentication sistemini kullan
        User user = ServiceManager.getInstance().getAuthService().login(username, password);
                
                if (user != null) {
            try {
                if (user.isAdmin()) {
                    SceneManager.getInstance().switchToAdminDashboard(currentStage, user);
                } else {
                    SceneManager.getInstance().switchToMainDashboard(currentStage, user);
                }
            } catch (Exception e) {
                setLoginLoading(false);
                showError("Hata", "Dashboard'a geÃ§iÅŸ baÅŸarÄ±sÄ±z: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            setLoginLoading(false);
            handleLoginFailure("GeÃ§ersiz kullanÄ±cÄ± adÄ± veya ÅŸifre!");
        }
    }
    
    // handleLoginSuccess metodunu kaldÄ±rdÄ±k - artÄ±k doÄŸrudan handleLogin'de yapÄ±yoruz
    
    /**
     * BaÅŸarÄ±sÄ±z login iÅŸlemini yÃ¶netir
     */
    private void handleLoginFailure(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        
        // Åifre alanÄ±nÄ± temizle
        passwordField.clear();
        passwordField.requestFocus();
        
        // HatalÄ± giriÅŸi gÃ¶ster
        usernameField.setStyle("-fx-border-color: red;");
        passwordField.setStyle("-fx-border-color: red;");
        
        // 3 saniye sonra border'larÄ± temizle
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                usernameField.setStyle("");
                passwordField.setStyle("");
            })
        );
        timeline.play();
    }
    
    /**
     * KayÄ±t ekranÄ±na geÃ§iÅŸ
     */
    @FXML
    public void handleRegister() {
        try {
            SceneManager.getInstance().switchToRegister(currentStage);
        } catch (Exception e) {
            showError("Ekran GeÃ§iÅŸ HatasÄ±", "KayÄ±t ekranÄ±na geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    /**
     * Login form validasyonu
     */
    private boolean validateLoginForm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            showError("Validasyon HatasÄ±", "KullanÄ±cÄ± adÄ± boÅŸ olamaz!");
            usernameField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Validasyon HatasÄ±", "Åifre boÅŸ olamaz!");
            passwordField.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            showError("Validasyon HatasÄ±", "KullanÄ±cÄ± adÄ± en az 3 karakter olmalÄ±dÄ±r!");
            usernameField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Loading durumunu ayarlar
     */
    private void setLoginLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        registerButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        
        if (loading) {
            statusLabel.setText("GiriÅŸ yapÄ±lÄ±yor...");
            statusLabel.setStyle("-fx-text-fill: blue;");
        }
    }
    
    /**
     * HatÄ±rlanan kullanÄ±cÄ± bilgilerini yÃ¼kle
     */
    private void loadRememberedCredentials() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            boolean rememberMe = prefs.getBoolean("remember_me", false);
            
            if (rememberMe) {
                String rememberedUsername = prefs.get("remembered_username", "");
                if (!rememberedUsername.isEmpty()) {
                    usernameField.setText(rememberedUsername);
                    rememberMeCheckBox.setSelected(true);
                    passwordField.requestFocus();
                }
            }
        } catch (Exception e) {
            System.err.println("KullanÄ±cÄ± bilgileri yÃ¼klenemedi: " + e.getMessage());
        }
    }
    
    @Override
    protected void clearForm() {
        usernameField.clear();
        passwordField.clear();
        rememberMeCheckBox.setSelected(false);
        statusLabel.setText("");
        usernameField.setStyle("");
        passwordField.setStyle("");
    }
    
    @Override
    protected boolean validateForm() {
        return validateLoginForm();
    }
    
    // ========================================
    // HOVER EFFECTS - Modern UI Ä°nteraksiyon
    // ========================================
    
    /**
     * Username field hover efekti
     */
    @FXML
    private void onUsernameFieldHover() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    @FXML
    private void onUsernameFieldExit() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    /**
     * Password field hover efekti
     */
    @FXML
    private void onPasswordFieldHover() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    @FXML
    private void onPasswordFieldExit() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    /**
     * Login button hover efekti
     */
    @FXML
    private void onLoginButtonHover() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    @FXML
    private void onLoginButtonExit() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    /**
     * Register button hover efekti
     */
    @FXML
    private void onRegisterButtonHover() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    @FXML
    private void onRegisterButtonExit() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    

    
    /**
     * Forgot password link hover efekti
     */
    @FXML
    private void onForgotPasswordHover() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    @FXML
    private void onForgotPasswordExit() {
        // Yeni tasarÄ±mda hover efekti yok
    }
    
    // ========================================
    // YENÄ° BUTON Ä°ÅLEVLERÄ°
    // ========================================
    

    
    /**
     * Åifremi unuttum iÅŸlemi
     */
    @FXML
    public void handleForgotPassword() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showInfo("Bilgi", "LÃ¼tfen Ã¶nce kullanÄ±cÄ± adÄ±nÄ±zÄ± girin, sonra 'Åifremi Unuttum' butonuna tÄ±klayÄ±n.");
            usernameField.requestFocus();
            return;
        }
        
        // Åifre sÄ±fÄ±rlama dialog'u gÃ¶ster
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Åifre SÄ±fÄ±rlama");
        alert.setHeaderText("Åifre SÄ±fÄ±rlama Talebi");
        alert.setContentText("KullanÄ±cÄ±: " + username + "\n\n" +
                            "Åifre sÄ±fÄ±rlama baÄŸlantÄ±sÄ± e-posta adresinize gÃ¶nderilecektir.\n" +
                            "E-postanÄ±zÄ± kontrol edin ve talimatlarÄ± takip edin.\n\n" +
                            "Not: Bu Ã¶zellik demo sÃ¼rÃ¼mÃ¼nde aktif deÄŸildir.");
        
        alert.showAndWait();
        
        statusLabel.setText("Åifre sÄ±fÄ±rlama talebi gÃ¶nderildi!");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
} 


