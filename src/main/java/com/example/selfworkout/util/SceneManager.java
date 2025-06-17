package com.example.selfworkout.util;

// Doğru controller paketlerini import edin
import com.example.selfworkout.controller.admin.AdminDashboardController;
import com.example.selfworkout.controller.user.UserDashboardController;
import com.example.selfworkout.controller.LoginController;
import com.example.selfworkout.controller.RegisterController;

// Ek içerik controller import'ları (eğer SceneManager bunları doğrudan cast ediyorsa)
import com.example.selfworkout.controller.ActivityLogContentController;
import com.example.selfworkout.controller.DashboardOverviewSimpleController;
import com.example.selfworkout.controller.EquipmentManagementContentController;
import com.example.selfworkout.controller.ExerciseManagementContentController;
import com.example.selfworkout.controller.MuscleGroupManagementContentController;
import com.example.selfworkout.controller.SystemReportsContentController;
import com.example.selfworkout.controller.UserManagementContentController;
import com.example.selfworkout.controller.UserProfileContentController;
import com.example.selfworkout.controller.WorkoutStartContentController;
import com.example.selfworkout.controller.WorkoutTrackingContentController;

import com.example.selfworkout.controller.BaseController; // BaseController import'u
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

    private SceneManager() {
        // Singleton constructor
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Yeni bir sahneyi yükler ve gösterir
     * @param stage Sahnenin gösterileceği Stage
     * @param fxmlPath Yüklenecek FXML dosyasının yolu (resources/com/example/selfworkout altındaki path)
     * @param title Pencere başlığı
     * @param user Yeni sahneye aktarılacak kullanıcı bilgisi (nullable)
     * @throws IOException FXML dosyası bulunamazsa veya yüklenemezse
     */
    private void loadScene(Stage stage, String fxmlPath, String title, User user) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(fxmlLoader.load()); // Boyutları FXML'den alacak

        // Stage ayarları
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(true); // Maksimize ayarı HelloApplication'da yapılıyor, burada esnek olsun.

        // Controller'a kullanıcı referansını aktar
        Object controller = fxmlLoader.getController();
        if (controller != null) {
            if (controller instanceof LoginController) {
                ((LoginController) controller).setStage(stage);
            } else if (controller instanceof RegisterController) {
                ((RegisterController) controller).setStage(stage);
            }
            // Admin ve User Dashboard controller'ları kendi paketlerinde
            else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setCurrentUser(user);
                ((AdminDashboardController) controller).setStage(stage);
            } else if (controller instanceof UserDashboardController) {
                ((UserDashboardController) controller).setCurrentUser(user);
                ((UserDashboardController) controller).setStage(stage);
            }
            // Diğer tüm BaseController tabanlı içerik controller'ları için genel kontrol
            else if (controller instanceof BaseController) {
                ((BaseController) controller).setStage(stage);
                ((BaseController) controller).setCurrentUser(user);
            }
            // Admin paneli içerik controller'ları için özel atamalar (eğer BaseController'dan miras almıyorlarsa veya özel bir durum varsa)
            else if (controller instanceof ActivityLogContentController) {
                ((ActivityLogContentController) controller).setCurrentUser(user);
            } else if (controller instanceof DashboardOverviewSimpleController) {
                ((DashboardOverviewSimpleController) controller).setCurrentUser(user);
            } else if (controller instanceof EquipmentManagementContentController) {
                ((EquipmentManagementContentController) controller).setCurrentUser(user);
            } else if (controller instanceof ExerciseManagementContentController) {
                ((ExerciseManagementContentController) controller).setCurrentUser(user);
            } else if (controller instanceof MuscleGroupManagementContentController) {
                ((MuscleGroupManagementContentController) controller).setCurrentUser(user);
            } else if (controller instanceof SystemReportsContentController) {
                ((SystemReportsContentController) controller).setCurrentUser(user);
            } else if (controller instanceof UserManagementContentController) {
                ((UserManagementContentController) controller).setCurrentUser(user);
            }
            // Kullanıcı paneli içerik controller'ları için özel atamalar
            else if (controller instanceof UserProfileContentController) {
                ((UserProfileContentController) controller).setCurrentUser(user);
            } else if (controller instanceof WorkoutStartContentController) {
                // WorkoutStartContentController'ın BaseController'dan miras almadığı için setParent metodu yok.
                // Bu durumda, eğer parent'a ihtiyaç duyuyorsa, o controller'ın initialize metodunda parent'ı alması gerekir.
                // Veya buraya cast ile setParent ekleyebilirsiniz, ancak bu biraz karmaşıklaşır.
                // Örneğin: if (controller instanceof WorkoutStartContentController) { ((WorkoutStartContentController) controller).setCurrentUser(user); ((WorkoutStartContentController) controller).setParent(this_is_parent_controller_reference_not_stage); }
            } else if (controller instanceof WorkoutTrackingContentController) {
                // Benzer şekilde WorkoutTrackingContentController için de
            }
        }

        // Tema uygulaması
        ThemeManager.getInstance().applyTheme(scene);

        // Pencereyi göster
        stage.show();
        Platform.runLater(() -> stage.setMaximized(true)); // Uygulamayı daima maksimize başlat
        System.out.println("✅ Sahne başarıyla yüklendi: " + fxmlPath);
    }

    // --- Sahne Geçiş Metotları ---

    public void switchToLogin(Stage stage) throws IOException {
        loadScene(stage, "/com/example/selfworkout/login.fxml", "SelfWorkout - Giriş", null);
    }

    public void switchToRegister(Stage stage) throws IOException {
        loadScene(stage, "/com/example/selfworkout/register.fxml", "SelfWorkout - Kayıt", null);
    }

    public void switchToAdminDashboard(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/admin-dashboard.fxml", "SelfWorkout - Admin Paneli", user);
    }

    public void switchToMainDashboard(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/main-dashboard.fxml", "SelfWorkout - Ana Panel", user);
    }

    // Ekran geçiş helper metotları
    public void switchToActivityLog(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/activity-log-content.fxml", "SelfWorkout - Aktivite Geçmişi", user);
    }

    public void switchToBodyStats(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/body-stats-content.fxml", "SelfWorkout - Vücut Ölçümleri", user);
    }

    public void switchToEquipmentManagement(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/equipment-management-content.fxml", "SelfWorkout - Ekipman Yönetimi", user);
    }

    public void switchToExerciseLibrary(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/exercise-library-content.fxml", "SelfWorkout - Egzersiz Kütüphanesi", user);
    }

    // Eğer exercise-management.fxml AdminDashboardController.java'ya bağlıysa ve içinde AdminDashboardController'ın içindeki contentArea'ya yüklenecekse yolu '/com/example/selfworkout/admin/exercise-management.fxml' olmalı
    public void switchToExerciseManagement(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/exercise-management-content.fxml", "SelfWorkout - Egzersiz Yönetimi", user);
    }

    public void switchToMuscleGroupManagement(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/muscle-group-management-content.fxml", "SelfWorkout - Kas Grubu Yönetimi", user);
    }

    public void switchToProgress(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/progress-content.fxml", "SelfWorkout - İlerleme Takibi", user);
    }

    public void switchToSystemReports(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/system-reports-content.fxml", "SelfWorkout - Sistem Raporları", user);
    }

    public void switchToUserManagement(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/admin/user-management-content.fxml", "SelfWorkout - Kullanıcı Yönetimi", user);
    }

    public void switchToUserProfile(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/user-profile-content.fxml", "SelfWorkout - Kullanıcı Profili", user);
    }

    public void switchToWorkoutStart(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/workout-start-content.fxml", "SelfWorkout - Antrenman Başlat", user);
    }

    public void switchToWorkoutTracking(Stage stage, User user) throws IOException {
        setCurrentUser(user);
        loadScene(stage, "/com/example/selfworkout/user/workout-tracking-content.fxml", "SelfWorkout - Antrenman Takibi", user);
    }


    // Logout işlemi
    public void logout(Stage stage) throws IOException {
        this.currentUser = null; // Kullanıcıyı sıfırla
        switchToLogin(stage);
    }
}