package com.example.selfworkout.controller.admin;

import com.example.selfworkout.controller.BaseController;
import com.example.selfworkout.controller.*; // Tüm controller'ları içermek için joker import
import com.example.selfworkout.model.User;
// import com.example.selfworkout.service.ServiceManager; // BaseController'da zaten var, gerek yok
import com.example.selfworkout.util.SceneManager; // SceneManager'ı kullanmak için
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController extends BaseController {

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

    private Button activeButton; // Track active navigation

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    @Override
    protected void initializeController() {
        System.out.println("🔄 AdminDashboardController initialize başlıyor...");

        setActiveButton(dashboardBtn);
        loadContent("/com/example/selfworkout/admin/dashboard-overview-simple.fxml");

        if (currentUser != null) {
            updateUserInfo();
        }

        System.out.println("✅ AdminDashboardController initialize tamamlandı.");
    }

    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        Platform.runLater(this::updateUserInfo);
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("🔄 Content yükleniyor: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            System.out.println("✅ FXML başarıyla yüklendi: " + fxmlPath);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            System.out.println("✅ Content area güncellendi");

            Object contentController = loader.getController();
            if (contentController instanceof BaseController) {
                BaseController baseContentController = (BaseController) contentController;
                baseContentController.setStage(currentStage);
                baseContentController.setCurrentUser(currentUser);
                baseContentController.setParent(this);
            } else {
                System.out.println("⚠️ Yüklenen FXML için controller bulunamadı veya BaseController'dan miras almıyor: " + fxmlPath);
                // Bu kısım, tüm içerik controller'larınız BaseController'dan miras almıyorsa önemlidir.
                // Şu anki durumda, AdminDashboard'da yüklenen tüm content controller'ların
                // (ActivityLogContentController, DashboardOverviewSimpleController, vb.)
                // BaseController'dan miras almadığı için burada özel olarak setCurrentUser metodunu çağıracağız.
                // İDEAL ÇÖZÜM: Tüm bu controller'ların BaseController'dan miras almasını sağlamaktır.

                if (contentController instanceof ActivityLogContentController) {
                    ((ActivityLogContentController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof DashboardOverviewSimpleController) {
                    ((DashboardOverviewSimpleController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof EquipmentManagementContentController) {
                    ((EquipmentManagementContentController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof ExerciseManagementContentController) {
                    ((ExerciseManagementContentController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof MuscleGroupManagementContentController) {
                    ((MuscleGroupManagementContentController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof SystemReportsContentController) {
                    ((SystemReportsContentController) contentController).setCurrentUser(currentUser);
                } else if (contentController instanceof UserManagementContentController) {
                    ((UserManagementContentController) contentController).setCurrentUser(currentUser);
                }
                // Eğer burada herhangi bir loadContent çağrısı UserProfileContentController, WorkoutStartContentController vb.
                // gibi user paketi altındaki bir controller'ı yüklemeye çalışıyorsa,
                // o zaman bu cast'lerin de doğru paket yoluyla yapılması gerekir.
                // Örnek: else if (contentController instanceof com.example.selfworkout.controller.user.UserProfileContentController) { ... }
            }

        } catch (IOException e) {
            System.err.println("❌ Content yüklenirken hata: " + fxmlPath);
            System.err.println("❌ Hata detayı: " + e.getMessage());
            e.printStackTrace();
            showError("Hata", "Sayfa yüklenirken hata oluştu: " + e.getMessage()); // DÜZELTİLDİ: showAlert yerine showError kullanıldı
        } catch (Exception e) {
            System.err.println("❌ Genel hata: " + e.getMessage());
            e.printStackTrace();
            showError("Hata", "Beklenmeyen hata: " + e.getMessage()); // DÜZELTİLDİ: showAlert yerine showError kullanıldı
        }
    }

    private void setActiveButton(Button button) {
        // Tüm navigation butonlarını buraya ekleyin
        Button[] navButtons = {dashboardBtn, userManagementBtn, exerciseManagementBtn,
                muscleGroupManagementBtn, equipmentManagementBtn,
                systemReportsBtn, activityLogBtn, userProfileBtn};

        for (Button btn : navButtons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif; -fx-font-size: 13px; -fx-padding: 0 16px; -fx-border-width: 0; -fx-cursor: hand; -fx-background-radius: 8px;");
            }
        }

        if (button != null) {
            button.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px; -fx-text-fill: white; -fx-font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 0 16px; -fx-border-width: 0; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 6, 0, 0, 2);");
            activeButton = button;
        }
    }

    @FXML
    private void handleDashboard() {
        System.out.println("🔄 Dashboard butonuna tıklandı");
        setActiveButton(dashboardBtn);
        loadContent("/com/example/selfworkout/admin/dashboard-overview-simple.fxml");
        System.out.println("🔄 Dashboard content loading başlatıldı");
    }

    @FXML
    private void handleUserManagement() {
        System.out.println("🔄 Kullanıcı Yönetimi butonuna tıklandı");
        setActiveButton(userManagementBtn);
        System.out.println("🔄 Aktif buton ayarlandı");
        loadContent("/com/example/selfworkout/admin/user-management-content.fxml");
        System.out.println("🔄 Content loading başlatıldı");
    }

    @FXML
    private void handleExerciseManagement() {
        setActiveButton(exerciseManagementBtn);
        loadContent("/com/example/selfworkout/admin/exercise-management-content.fxml");
    }

    @FXML
    private void handleMuscleGroupManagement() {
        setActiveButton(muscleGroupManagementBtn);
        loadContent("/com/example/selfworkout/admin/muscle-group-management-content.fxml");
    }

    @FXML
    private void handleEquipmentManagement() {
        setActiveButton(equipmentManagementBtn);
        loadContent("/com/example/selfworkout/admin/equipment-management-content.fxml");
    }

    @FXML
    private void handleSystemReports() {
        setActiveButton(systemReportsBtn);
        loadContent("/com/example/selfworkout/admin/system-reports-content.fxml");
    }

    @FXML
    private void handleActivityLog() {
        System.out.println("🔄 Aktivite Geçmişi butonuna tıklandı");
        setActiveButton(activityLogBtn);
        loadContent("/com/example/selfworkout/admin/activity-log-content.fxml");
        System.out.println("🔄 Activity log content loading başlatıldı");
    }

    @FXML
    private void handleUserProfile() {
        System.out.println("🔄 Kullanıcı Profili butonuna tıklandı");
        // Not: AdminDashboardController içindeki StackPane'e user-profile-content.fxml
        // direkt yüklenemez, çünkü o dosya User paketi altındadır ve kendi controller'ı vardır.
        // Admin'in kendi profilini düzenlemesi için UserManagementContentController kullanılabilir.
        setActiveButton(userManagementBtn);
        loadContent("/com/example/selfworkout/admin/user-management-content.fxml");
        showInfo("Bilgi", "Admin panelinde profil düzenleme, Kullanıcı Yönetimi bölümündedir."); // DÜZELTİLDİ: showAlert yerine showInfo kullanıldı
    }

    @FXML
    private void handleLogout() {
        try {
            serviceManager.getAuthenticationService().logout();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            SceneManager.getInstance().logout(stage);
        } catch (Exception e) {
            showError("Hata", "Çıkış sırasında hata oluştu: " + e.getMessage()); // DÜZELTİLDİ: showAlert yerine showError kullanıldı
        }
    }

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
}