package com.example.selfworkout.controller.admin;

import com.example.selfworkout.controller.*;
import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import com.example.selfworkout.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button userManagementBtn;
    @FXML private Button exerciseManagementBtn;
    @FXML private Button muscleGroupManagementBtn;
    @FXML private Button equipmentManagementBtn;
    @FXML private Button systemReportsBtn;
    @FXML private Button activityLogBtn;
    @FXML private Button logoutBtn;
    
    // User Profile Elements
    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button userProfileBtn;
    
    // Content Area
    @FXML private StackPane contentArea;
    
    // Dashboard Elements (removed - dashboard cards are in separate content)
    
    // Services
    private ServiceManager serviceManager;
    private User currentUser;
    private Button activeButton; // Track active navigation
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        
        // Dashboard aktif olarak başla
        setActiveButton(dashboardBtn);
        
        // Chart problemsiz basit dashboard'ı yükle
        loadContent("dashboard-overview-simple.fxml");
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            currentUser = serviceManager.getAuthenticationService().getCurrentUser();
            
            if (currentUser != null) {
                // Kullanıcı bilgilerini sol alt panelde göster
                updateUserInfo();
            }
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard Service initialization hatası: " + e.getMessage());
        }
    }
    
    /**
     * Content area'ya FXML dosyası yükle
     */
    private void loadContent(String fxmlPath) {
        try {
            System.out.println("🔄 Content yükleniyor: " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/selfworkout/" + fxmlPath));
            Node content = loader.load();
            
            System.out.println("✅ FXML başarıyla yüklendi: " + fxmlPath);
            
            // Content area'yı temizle ve yeni içeriği yükle
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            System.out.println("✅ Content area güncellendi");
            
            // Controller'a currentUser'ı set et (varsa)
            Object controller = loader.getController();
            if (controller != null) {
                System.out.println("✅ Controller alındı: " + controller.getClass().getSimpleName());
                setCurrentUserOnController(controller);
            } else {
                System.out.println("⚠️ Controller null");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Content yüklenirken hata: " + fxmlPath);
            System.err.println("❌ Hata detayı: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "Sayfa yüklenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Genel hata: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "Beklenmeyen hata: " + e.getMessage());
        }
    }
    
    /**
     * Controller'a current user'ı set et
     */
    private void setCurrentUserOnController(Object controller) {
        if (currentUser != null && controller != null) {
            try {
                // Farklı controller türleri için setCurrentUser çağır
                if (controller instanceof com.example.selfworkout.controller.ExerciseManagementController) {
                    ((ExerciseManagementController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.ExerciseManagementContentController) {
                    ((ExerciseManagementContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.SystemReportsContentController) {
                    ((SystemReportsContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.UserManagementContentController) {
                    ((UserManagementContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.DashboardOverviewSimpleController) {
                    ((DashboardOverviewSimpleController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.MuscleGroupManagementContentController) {
                    ((MuscleGroupManagementContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.EquipmentManagementContentController) {
                    ((EquipmentManagementContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.UserProfileContentController) {
                    ((UserProfileContentController) controller).setCurrentUser(currentUser);
                }
                else if (controller instanceof com.example.selfworkout.controller.ActivityLogContentController) {
                    ((ActivityLogContentController) controller).setCurrentUser(currentUser);
                }
                // Diğer controller türleri için gerektiğinde eklenebilir
            } catch (Exception e) {
                System.err.println("Controller'a currentUser set edilirken hata: " + e.getMessage());
            }
        }
    }
    
    /**
     * Aktif navigation butonunu set et (style değişimi için)
     */
    private void setActiveButton(Button button) {
        // Önceki aktif butonu normal stile çevir
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif; -fx-font-size: 13px; -fx-padding: 0 16px; -fx-border-width: 0; -fx-cursor: hand; -fx-background-radius: 8px;");
        }
        
        // Yeni aktif butonu aktif stile çevir
        button.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px; -fx-text-fill: white; -fx-font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 0 16px; -fx-border-width: 0; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 6, 0, 0, 2);");
        activeButton = button;
    }
    
    // Navigation Event Handlers
    
    @FXML
    private void handleDashboard() {
        System.out.println("🔄 Dashboard butonuna tıklandı");
        setActiveButton(dashboardBtn);
        loadContent("dashboard-overview-simple.fxml");
        System.out.println("🔄 Dashboard content loading başlatıldı");
    }
    
    @FXML
    private void handleUserManagement() {
        System.out.println("🔄 Kullanıcı Yönetimi butonuna tıklandı");
        setActiveButton(userManagementBtn);
        System.out.println("🔄 Aktif buton ayarlandı");
        loadContent("user-management-content.fxml");
        System.out.println("🔄 Content loading başlatıldı");
    }
    
    @FXML
    private void handleExerciseManagement() {
        setActiveButton(exerciseManagementBtn);
        loadContent("exercise-management-content.fxml");
    }
    
    @FXML
    private void handleMuscleGroupManagement() {
        setActiveButton(muscleGroupManagementBtn);
        loadContent("muscle-group-management-content.fxml");
    }
    
    @FXML
    private void handleEquipmentManagement() {
        setActiveButton(equipmentManagementBtn);
        loadContent("equipment-management-content.fxml");
    }
    
    @FXML
    private void handleSystemReports() {
        setActiveButton(systemReportsBtn);
        loadContent("system-reports-content.fxml");
    }
    
    @FXML
    private void handleActivityLog() {
        System.out.println("🔄 Aktivite Geçmişi butonuna tıklandı");
        setActiveButton(activityLogBtn);
        loadContent("activity-log-content.fxml");
        System.out.println("🔄 Activity log content loading başlatıldı");
    }
    
    // User Profile Event Handlers
    
    @FXML
    private void handleUserProfile() {
        System.out.println("🔄 Kullanıcı Profili butonuna tıklandı");
        setActiveButton(userManagementBtn); // Kullanıcı Yönetimi butonunu aktif yap
        loadContent("user-management-content.fxml");
        System.out.println("🔄 User Management content loading başlatıldı");
    }
    
    @FXML
    private void handleLogout() {
        try {
            serviceManager.getAuthenticationService().logout();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            SceneManager.getInstance().logout(stage);
        } catch (Exception e) {
            showAlert("Hata", "Çıkış sırasında hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Kullanıcı bilgilerini güncelle
     */
    private void updateUserInfo() {
        if (currentUser != null) {
            if (userFullNameLabel != null) {
                userFullNameLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            }
            
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "Admin");
            }
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }
    
    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Alert gösterme hatası: " + e.getMessage());
        }
    }
} 




