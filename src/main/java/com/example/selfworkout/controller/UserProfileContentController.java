package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * KullanÄ±cÄ± profil yÃ¶netimi controller sÄ±nÄ±fÄ±
 * KullanÄ±cÄ±larÄ±n kendi profil bilgilerini gÃ¶rÃ¼ntÃ¼lemesi ve dÃ¼zenlemesi iÃ§in
 */
public class UserProfileContentController implements Initializable {

    // Header Elements
    @FXML private Label userInfoLabel;
    @FXML private Label lastUpdateLabel;
    
    // Profile Display Elements
    @FXML private Label profileAvatarLabel;
    @FXML private Label displayNameLabel;
    @FXML private Label roleDisplayLabel;
    @FXML private Label memberSinceLabel;
    
    // Account Information Fields
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    
    // Personal Information Fields
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private DatePicker birthdateField;
    
    // Password Change Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Action Buttons
    @FXML private Button updateProfileBtn;
    @FXML private Button resetBtn;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Message Elements
    @FXML private VBox messageContainer;
    @FXML private Label messageLabel;
    
    // Services and Data
    private ServiceManager serviceManager;
    private UserService userService;
    private AuthenticationService authService;
    private User currentUser;
    private User originalUserData; // Backup for reset functionality
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Location kontrolÃ¼ ekle
        if (location == null) {
            System.err.println("âš ï¸ UserProfileContentController initialize: Location is not set!");
            return;
        }
        
        initializeServices();
        setupUI();
        loadUserProfile();
    }
    
    /**
     * Servisleri baÅŸlatÄ±r
     */
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            authService = serviceManager.getAuthenticationService();
            userService = serviceManager.getUserService();
            currentUser = authService.getCurrentUser();
        } catch (Exception e) {
            System.err.println("âŒ UserProfile Service initialization hatasÄ±: " + e.getMessage());
            showError("Servis baÅŸlatma hatasÄ±: " + e.getMessage());
        }
    }
    
    /**
     * UI bileÅŸenlerini ayarlar
     */
    private void setupUI() {
        // Loading indicator'Ä± baÅŸlangÄ±Ã§ta gizle
        loadingIndicator.setVisible(false);
        messageContainer.setVisible(false);
        
        // Form validasyon listener'larÄ± ekle
        setupValidationListeners();
    }
    
    /**
     * Form validasyon listener'larÄ±nÄ± ayarlar
     */
    private void setupValidationListeners() {
        // Email validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                if (!isValidEmail(newVal.trim())) {
                    emailField.setStyle(emailField.getStyle() + "-fx-border-color: #e74c3c;");
                } else {
                    emailField.setStyle(emailField.getStyle().replace("-fx-border-color: #e74c3c;", ""));
                }
            }
        });
        
        // Password confirmation validation
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            String newPassword = newPasswordField.getText();
            if (newVal != null && !newVal.isEmpty() && !newVal.equals(newPassword)) {
                confirmPasswordField.setStyle(confirmPasswordField.getStyle() + "-fx-border-color: #e74c3c;");
            } else {
                confirmPasswordField.setStyle(confirmPasswordField.getStyle().replace("-fx-border-color: #e74c3c;", ""));
            }
        });
    }
    
    /**
     * KullanÄ±cÄ± profil verilerini yÃ¼kler
     */
    private void loadUserProfile() {
        if (currentUser == null) {
            showError("KullanÄ±cÄ± oturumu bulunamadÄ±!");
            return;
        }
        
        setLoading(true);
        
        Task<User> loadTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                // GÃ¼ncel profil bilgilerini veritabanÄ±ndan al
                return userService.getUserProfile();
            }
            
            @Override
            protected void succeeded() {
                setLoading(false);
                User userData = getValue();
                if (userData != null) {
                    currentUser = userData;
                    originalUserData = cloneUser(userData); // Backup for reset
                    updateUIWithUserData(userData);
                } else {
                    showError("Profil bilgileri yÃ¼klenemedi!");
                }
            }
            
            @Override
            protected void failed() {
                setLoading(false);
                showError("Profil yÃ¼kleme hatasÄ±: " + getException().getMessage());
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * UI'yi kullanÄ±cÄ± verileri ile gÃ¼nceller
     */
    private void updateUIWithUserData(User user) {
        Platform.runLater(() -> {
            // Profile display update
            displayNameLabel.setText(user.getFullName() != null && !user.getFullName().trim().isEmpty() 
                ? user.getFullName() : user.getUsername());
            
            // Role display
            if (user.isAdmin()) {
                profileAvatarLabel.setText("ğŸ‘‘");
                roleDisplayLabel.setText("ğŸ”‘ YÃ¶netici");
            } else {
                profileAvatarLabel.setText("ğŸ‘¤");
                roleDisplayLabel.setText("ğŸ‘¤ KullanÄ±cÄ±");
            }
            
            // Member since
            if (user.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                memberSinceLabel.setText("Ãœyelik: " + user.getCreatedAt().format(formatter) + " tarihinden beri");
            }
            
            // Form fields
            usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
            emailField.setText(user.getEmail() != null ? user.getEmail() : "");
            nameField.setText(user.getName() != null ? user.getName() : "");
            surnameField.setText(user.getSurname() != null ? user.getSurname() : "");
            
            if (user.getBirthdate() != null) {
                birthdateField.setValue(user.getBirthdate());
            }
            
            // Info messages
            userInfoLabel.setText("Merhaba " + (user.getFullName() != null ? user.getFullName() : user.getUsername()) + 
                ", profil bilgilerinizi gÃ¼ncelleyebilirsiniz");
        });
    }
    
    /**
     * Profil gÃ¼ncelleme iÅŸlemi
     */
    @FXML
    private void handleUpdateProfile() {
        if (!validateForm()) {
            return;
        }
        
        setLoading(true);
        hideMessage();
        
        Task<Boolean> updateTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // KiÅŸisel bilgileri gÃ¼ncelle
                boolean profileUpdated = userService.updateProfile(
                    nameField.getText().trim(),
                    surnameField.getText().trim(),
                    emailField.getText().trim(),
                    birthdateField.getValue()
                );
                
                // Åifre deÄŸiÅŸikliÄŸi gerekli mi?
                if (!currentPasswordField.getText().isEmpty() && 
                    !newPasswordField.getText().isEmpty()) {
                    
                    // Åifre deÄŸiÅŸtir
                    boolean passwordChanged = authService.changePassword(
                        currentPasswordField.getText(),
                        newPasswordField.getText()
                    );
                    
                    return profileUpdated && passwordChanged;
                }
                
                return profileUpdated;
            }
            
            @Override
            protected void succeeded() {
                setLoading(false);
                Platform.runLater(() -> {
                    if (getValue()) {
                        showSuccess("âœ… Profil baÅŸarÄ±yla gÃ¼ncellendi!");
                        clearPasswordFields();
                        
                        // GÃ¼ncel verileri tekrar yÃ¼kle
                        loadUserProfile();
                        
                        // BaÅŸarÄ± mesajÄ±nÄ± gÃ¶ster ve sonra gizle
                        Timeline.runLater(3000, () -> hideMessage());
                    } else {
                        showError("âŒ Profil gÃ¼ncellenemedi! LÃ¼tfen bilgilerinizi kontrol edin.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                setLoading(false);
                Platform.runLater(() -> {
                    showError("âŒ GÃ¼ncelleme hatasÄ±: " + getException().getMessage());
                });
            }
        };
        
        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    /**
     * Formu sÄ±fÄ±rla (orijinal verilere dÃ¶ndÃ¼r)
     */
    @FXML
    private void handleReset() {
        if (originalUserData != null) {
            updateUIWithUserData(originalUserData);
        }
        clearPasswordFields();
        hideMessage();
        showInfo("ğŸ”„ Form orijinal deÄŸerlere sÄ±fÄ±rlandÄ±");
        Timeline.runLater(2000, () -> hideMessage());
    }
    
    /**
     * Form validasyonu
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errors.append("â€¢ Email adresi gereklidir\n");
        } else if (!isValidEmail(email)) {
            errors.append("â€¢ GeÃ§erli bir email adresi girin\n");
        }
        
        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            errors.append("â€¢ Ad alanÄ± gereklidir\n");
        }
        
        if (surnameField.getText().trim().isEmpty()) {
            errors.append("â€¢ Soyad alanÄ± gereklidir\n");
        }
        
        // Password validation (if changing password)
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                errors.append("â€¢ Åifre deÄŸiÅŸtirmek iÃ§in mevcut ÅŸifrenizi girin\n");
            }
            if (newPassword.isEmpty()) {
                errors.append("â€¢ Yeni ÅŸifre gereklidir\n");
            } else if (newPassword.length() < 6) {
                errors.append("â€¢ Yeni ÅŸifre en az 6 karakter olmalÄ±dÄ±r\n");
            }
            if (!newPassword.equals(confirmPassword)) {
                errors.append("â€¢ Yeni ÅŸifre tekrarÄ± eÅŸleÅŸmiyor\n");
            }
        }
        
        if (errors.length() > 0) {
            showError("LÃ¼tfen aÅŸaÄŸÄ±daki hatalarÄ± dÃ¼zeltin:\n\n" + errors.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Email validasyonu
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Åifre alanlarÄ±nÄ± temizle
     */
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    /**
     * User kopyalama (backup iÃ§in)
     */
    private User cloneUser(User user) {
        User clone = new User();
        clone.setId(user.getId());
        clone.setUsername(user.getUsername());
        clone.setEmail(user.getEmail());
        clone.setName(user.getName());
        clone.setSurname(user.getSurname());
        clone.setBirthdate(user.getBirthdate());
        clone.setRole(user.getRole());
        clone.setCreatedAt(user.getCreatedAt());
        return clone;
    }
    
    /**
     * Loading durumunu ayarla
     */
    private void setLoading(boolean isLoading) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(isLoading);
            updateProfileBtn.setDisable(isLoading);
            resetBtn.setDisable(isLoading);
        });
    }
    
    /**
     * BaÅŸarÄ± mesajÄ± gÃ¶ster
     */
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #27ae60;");
            messageContainer.setVisible(true);
        });
    }
    
    /**
     * Hata mesajÄ± gÃ¶ster
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #e74c3c;");
            messageContainer.setVisible(true);
        });
    }
    
    /**
     * Bilgi mesajÄ± gÃ¶ster
     */
    private void showInfo(String message) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #3498db;");
            messageContainer.setVisible(true);
        });
    }
    
    /**
     * MesajÄ± gizle
     */
    private void hideMessage() {
        Platform.runLater(() -> {
            messageContainer.setVisible(false);
        });
    }
    
    /**
     * Current user'Ä± set et
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("âœ… UserProfile: Current user set to " + user.getUsername());
            // EÄŸer initialize'dan sonra user set edilirse tekrar yÃ¼kle
            if (serviceManager != null) {
                loadUserProfile();
            }
        }
    }
    
    /**
     * Timeline utility class for delayed actions
     */
    private static class Timeline {
        public static void runLater(int delayMs, Runnable action) {
            Task<Void> delayTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(delayMs);
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(action);
                }
            };
            
            Thread delayThread = new Thread(delayTask);
            delayThread.setDaemon(true);
            delayThread.start();
        }
    }
} 



