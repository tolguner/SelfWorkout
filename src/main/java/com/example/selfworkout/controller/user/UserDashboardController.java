package com.example.selfworkout.controller.user;

import com.example.selfworkout.controller.BaseController;
import com.example.selfworkout.controller.UserDashboardContentController;
import com.example.selfworkout.model.User;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserDashboardController implements Initializable {

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button workoutBtn;
    @FXML private Button exerciseLibraryBtn;
    @FXML private Button progressBtn;
    @FXML private Button bodyStatsBtn;
    @FXML private Button goalsBtn;
    @FXML private Button profileBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;

    // User Info Labels
    @FXML private Label userFullNameLabel;
    @FXML private Label userLevelLabel;

    // Quick Stats in sidebar
    @FXML private Label weeklyWorkoutsLabel;
    @FXML private Label weeklyTimeLabel;

    // Content Area
    @FXML private StackPane contentArea;

    // Services
    private UserService userService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ UserDashboard initialize başlıyor");
        
        initializeServices();
        setupNavigationButtons();
        loadDefaultContent();
        
        System.out.println("✅ UserDashboard initialize tamamlandı");
    }

    private void initializeServices() {
        try {
            this.userService = ServiceManager.getInstance().getUserService();
            System.out.println("✅ UserDashboard services initialized");
        } catch (Exception e) {
            System.err.println("❌ UserDashboard Service initialization hatası: " + e.getMessage());
        }
    }

    private void setupNavigationButtons() {
        // Set active button style
        setActiveButton(dashboardBtn);
        
        // Load sidebar stats
        loadSidebarStats();
    }

    private void loadSidebarStats() {
        // Demo veriler - gerçek uygulamada veritabanından gelecek
        weeklyWorkoutsLabel.setText("3");
        weeklyTimeLabel.setText("4h");
    }

    private void loadDefaultContent() {
        // Dashboard content'i yükle
        loadContent("user-dashboard-content.fxml");
    }

    // Navigation Methods
    @FXML
    private void handleDashboard() {
        setActiveButton(dashboardBtn);
        loadContent("user-dashboard-content.fxml");
    }

    @FXML
    private void handleWorkout() {
        setActiveButton(workoutBtn);
        loadContent("workout-start-content.fxml");
    }

    @FXML
    public void handleExerciseLibrary() {
        setActiveButton(exerciseLibraryBtn);
        loadContent("exercise-library-content.fxml");
    }

    @FXML
    public void handleProgress() {
        setActiveButton(progressBtn);
        loadContent("progress-content.fxml");
    }

    @FXML
    public void handleBodyStats() {
        setActiveButton(bodyStatsBtn);
        loadContent("body-stats-content.fxml");
    }

    @FXML
    public void handleGoals() {
        setActiveButton(goalsBtn);
        // TODO: goals-content.fxml dosyası henüz oluşturulmadı
        showAlert("Bilgi", "Hedefler sayfası yakında eklenecek!");
    }

    @FXML
    private void handleProfile() {
        setActiveButton(profileBtn);
        loadContent("user-profile-content.fxml");
    }

    @FXML
    public void handleSettings() {
        setActiveButton(settingsBtn);
        // TODO: user-settings-content.fxml dosyası henüz oluşturulmadı
        showAlert("Bilgi", "Ayarlar sayfası yakında eklenecek!");
    }

    @FXML
    private void handleLogout() {
        try {
            // Çıkış onayı
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Çıkış Onayı");
            alert.setHeaderText("Çıkış yapmak istediğinizden emin misiniz?");
            alert.setContentText("Tüm oturumunuz sonlandırılacak.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Login sayfasına dön
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/selfworkout/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("SelfWorkout - Giriş");
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Logout hatası: " + e.getMessage());
            showAlert("Hata", "Çıkış yapılırken bir hata oluştu.");
        }
    }

    private void setActiveButton(Button activeButton) {
        // Tüm butonları normal stile çevir
        Button[] buttons = {dashboardBtn, workoutBtn, exerciseLibraryBtn, progressBtn, bodyStatsBtn, goalsBtn, profileBtn, settingsBtn};
        
        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-padding: 0 16px; -fx-border-width: 0; -fx-cursor: hand; -fx-background-radius: 8px;");
            }
        }
        
        // Aktif butonu vurgula
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 16px; -fx-border-width: 0; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 6, 0, 0, 2);");
        }
    }

    private void loadContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/selfworkout/" + fxmlFile));
            Parent content = loader.load();
            
            // Controller'a kullanıcı bilgisini aktar
            Object controller = loader.getController();
            if (controller instanceof com.example.selfworkout.controller.UserDashboardContentController && currentUser != null) {
                UserDashboardContentController dashboardController = (UserDashboardContentController) controller;
                dashboardController.setCurrentUser(currentUser);
                dashboardController.setParentController(this); // Parent referansını set et
            } else if (controller instanceof com.example.selfworkout.controller.BaseController && currentUser != null) {
                BaseController baseController = (BaseController) controller;
                baseController.setCurrentUser(currentUser);
                baseController.setParent(this); // Parent referansını set et
            }
            
            // Content area'yı temizle ve yeni içeriği ekle
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            System.out.println("✅ Content yüklendi: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("❌ Content yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "İçerik yüklenirken bir hata oluştu.");
        }
    }

    /**
     * Kullanıcı bilgilerini günceller
     */
    public void updateUserInfo() {
        if (currentUser != null) {
            userFullNameLabel.setText(currentUser.getFullName());
            userLevelLabel.setText("Sporcu");
            
            // Sidebar istatistikleri güncelle
            loadSidebarStats();
        }
    }

    /**
     * Workout başlatma ekranına geçiş yapar
     */
    public void loadWorkoutStartContent() {
        setActiveButton(workoutBtn);
        loadContent("workout-start-content.fxml");
    }

    /**
     * Workout takip ekranına geçiş yapar
     */
    public void loadWorkoutTrackingContent() {
        loadContent("workout-tracking-content.fxml");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        if (user != null) {
            System.out.println("✅ Kullanıcı bilgileri set edildi: " + user.getUsername());
            
            // Kullanıcı bilgilerini güncelle
            Platform.runLater(this::updateUserInfo);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 




