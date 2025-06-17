package com.example.selfworkout.controller.user;

import com.example.selfworkout.controller.BaseController;
import com.example.selfworkout.controller.*; // Tüm controller'ları içermek için joker import
import com.example.selfworkout.model.User;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.UserService;
import com.example.selfworkout.service.WorkoutService;
import com.example.selfworkout.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView; // Kullanılmıyorsa kaldırılabilir
import javafx.scene.layout.AnchorPane; // Kullanılmıyorsa kaldırılabilir
import javafx.scene.layout.BorderPane; // Kullanılmıyorsa kaldırılabilir
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.util.List;

public class UserDashboardController extends BaseController {

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

    // Services (BaseController'dan geliyor, burada tekrar tanımlamaya gerek yok)
    private WorkoutService workoutService;
    private Button activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources); // BaseController'ın initialize metodunu çağırır.
    }

    @Override
    protected void initializeController() {
        System.out.println("✅ UserDashboardController initialize başlıyor");

        // Bu service, sidebar istatistikleri için burada alınmalı
        this.workoutService = serviceManager.getWorkoutService();

        setupNavigationButtons();
        loadDefaultContent();

        if (currentUser != null) {
            updateUserInfo();
        }

        System.out.println("✅ UserDashboardController initialize tamamlandı");
    }

    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        Platform.runLater(this::updateUserInfo);
    }

    private void setupNavigationButtons() {
        setActiveButton(dashboardBtn);
    }

    private void loadSidebarStats() {
        if (currentUser == null || workoutService == null) {
            if (weeklyWorkoutsLabel != null) weeklyWorkoutsLabel.setText("0");
            if (weeklyTimeLabel != null) weeklyTimeLabel.setText("0h");
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            // BaseController'da ServiceManager zaten var, onun üzerinden WorkoutService'e eriş.
            List<com.example.selfworkout.model.UserWorkout> weeklyWorkouts =
                    serviceManager.getWorkoutService().getAllWorkouts().stream()
                            .filter(w -> w.getUserId() == currentUser.getId())
                            .filter(w -> w.getWorkoutDate() != null && !w.getWorkoutDate().isBefore(startOfWeek) && !w.getWorkoutDate().isAfter(endOfWeek))
                            .filter(w -> "completed".equals(w.getStatus()))
                            .collect(java.util.stream.Collectors.toList());

            int totalWeeklyWorkouts = weeklyWorkouts.size();
            int totalWeeklyDurationMinutes = weeklyWorkouts.stream()
                    .mapToInt(w -> w.getDurationMinutes() != null ? w.getDurationMinutes() : 0)
                    .sum();

            Platform.runLater(() -> {
                if (weeklyWorkoutsLabel != null) {
                    weeklyWorkoutsLabel.setText(String.valueOf(totalWeeklyWorkouts));
                }
                if (weeklyTimeLabel != null) {
                    weeklyTimeLabel.setText(String.format("%dh %02dm", totalWeeklyDurationMinutes / 60, totalWeeklyDurationMinutes % 60));
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Sidebar istatistikleri yüklenirken hata: " + e.getMessage());
            Platform.runLater(() -> {
                if (weeklyWorkoutsLabel != null) weeklyWorkoutsLabel.setText("0");
                if (weeklyTimeLabel != null) weeklyTimeLabel.setText("0h");
            });
        }
    }


    private void loadDefaultContent() {
        loadContent("/com/example/selfworkout/user/user-dashboard-content.fxml");
    }

    // Navigation Methods
    @FXML
    private void handleDashboard() {
        setActiveButton(dashboardBtn);
        loadContent("/com/example/selfworkout/user/user-dashboard-content.fxml");
    }

    @FXML
    private void handleWorkout() {
        setActiveButton(workoutBtn);
        loadContent("/com/example/selfworkout/user/workout-start-content.fxml");
    }

    @FXML
    public void handleExerciseLibrary() {
        setActiveButton(exerciseLibraryBtn);
        loadContent("/com/example/selfworkout/user/exercise-library-content.fxml");
    }

    @FXML
    public void handleProgress() {
        setActiveButton(progressBtn);
        loadContent("/com/example/selfworkout/user/progress-content.fxml");
    }

    @FXML
    public void handleBodyStats() {
        setActiveButton(bodyStatsBtn);
        loadContent("/com/example/selfworkout/user/body-stats-content.fxml");
    }

    @FXML
    public void handleGoals() {
        setActiveButton(goalsBtn);
        // DÜZELTİLDİ: showAlert yerine showInfo kullanıldı
        showInfo("Bilgi", "Hedefler sayfası yakında eklenecek!");
    }

    @FXML
    private void handleProfile() {
        setActiveButton(profileBtn);
        loadContent("/com/example/selfworkout/user/user-profile-content.fxml");
    }

    @FXML
    public void handleSettings() {
        setActiveButton(settingsBtn);
        // DÜZELTİLDİ: showAlert yerine showInfo kullanıldı
        showInfo("Bilgi", "Ayarlar sayfası yakında eklenecek!");
    }

    @FXML
    private void handleLogout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Çıkış Onayı");
            alert.setHeaderText("Çıkış yapmak istediğinizden emin misiniz?");
            alert.setContentText("Tüm oturumunuz sonlandırılacak.");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                serviceManager.getAuthenticationService().logout();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                SceneManager.getInstance().logout(stage);
            }
        } catch (IOException e) {
            System.err.println("Logout hatası: " + e.getMessage());
            showError("Hata", "Çıkış yapılırken bir hata oluştu."); // DÜZELTİLDİ: showAlert yerine showError kullanıldı
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {dashboardBtn, workoutBtn, exerciseLibraryBtn, progressBtn, bodyStatsBtn, goalsBtn, profileBtn, settingsBtn};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-padding: 0 16px; -fx-border-width: 0; -fx-cursor: hand; -fx-background-radius: 8px;");
            }
        }

        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 16px; -fx-border-width: 0; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 6, 0, 0, 2);");
            this.activeButton = activeButton; // class field'a ata
        }
    }

    private void loadContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();

            Object contentController = loader.getController();
            if (contentController instanceof BaseController) {
                BaseController baseContentController = (BaseController) contentController;
                baseContentController.setStage(currentStage);
                baseContentController.setCurrentUser(currentUser);
                baseContentController.setParent(this);
            } else {
                System.out.println("⚠️ Yüklenen FXML için controller bulunamadı veya BaseController'dan miras almıyor: " + fxmlFile);
                // Eğer bazı content controller'lar BaseController'dan miras almıyorsa,
                // ve setCurrentUser, setStage gibi metotları varsa, burada özel cast'ler yapabilirsiniz.
                // Örneğin:
                if (contentController instanceof UserDashboardContentController) {
                    ((UserDashboardContentController) contentController).setCurrentUser(currentUser);
                    ((UserDashboardContentController) contentController).setParentController(this); // Kendi özel metodu
                }
                else if (contentController instanceof WorkoutStartContentController) {
                    ((WorkoutStartContentController) contentController).setCurrentUser(currentUser);
                    ((WorkoutStartContentController) contentController).setParent(this);
                }
                else if (contentController instanceof WorkoutTrackingContentController) {
                    ((WorkoutTrackingContentController) contentController).setCurrentUser(currentUser);
                    ((WorkoutTrackingContentController) contentController).setParent(this);
                }
                // Diğer tüm özel controller'lar buraya eklenebilir.
                // İDEAL OLAN: Tüm içerik controller'ların BaseController'dan miras alması ve bu if-else bloğunun kısalmasıdır.
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            System.out.println("✅ Content yüklendi: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("❌ Content yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
            showError("Hata", "İçerik yüklenirken bir hata oluştu."); // DÜZELTİLDİ: showAlert yerine showError kullanıldı
        }
    }

    /**
     * Kullanıcı bilgilerini günceller (sidebar için)
     */
    public void updateUserInfo() {
        if (currentUser != null) {
            if (userFullNameLabel != null) {
                userFullNameLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            }
            if (userLevelLabel != null) {
                userLevelLabel.setText(currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "Sporcu");
            }
            // Sidebar istatistikleri güncelle
            loadSidebarStats();
        }
    }

    /**
     * Workout başlatma ekranına geçiş yapar
     */
    public void loadWorkoutStartContent() {
        setActiveButton(workoutBtn);
        loadContent("/com/example/selfworkout/user/workout-start-content.fxml");
    }

    /**
     * Workout takip ekranına geçiş yapar
     */
    public void loadWorkoutTrackingContent() {
        loadContent("/com/example/selfworkout/user/workout-tracking-content.fxml");
    }
}