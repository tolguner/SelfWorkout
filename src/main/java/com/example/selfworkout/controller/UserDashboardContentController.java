package com.example.selfworkout.controller;

import com.example.selfworkout.controller.user.UserDashboardController;
import com.example.selfworkout.model.User;
import com.example.selfworkout.model.UserWorkout;
import com.example.selfworkout.model.BodyStats;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.UserService;
import com.example.selfworkout.service.WorkoutService;
import com.example.selfworkout.service.BodyStatsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class UserDashboardContentController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label motivationLabel;
    @FXML private Label totalWorkoutsLabel;
    @FXML private Label monthlyWorkoutsLabel;
    @FXML private Label currentBMILabel;
    @FXML private Label goalProgressLabel;
    @FXML private Label dailyTipLabel;
    @FXML private ListView<String> recentWorkoutsList;
    @FXML private LineChart<String, Number> progressChart;
    @FXML private CategoryAxis progressXAxis;
    @FXML private NumberAxis progressYAxis;
    
    // HÄ±zlÄ± eylem butonlarÄ±
    @FXML private Button startWorkoutBtn;
    @FXML private Button trackWorkoutBtn;
    @FXML private Button addBodyStatsBtn;
    @FXML private Button viewProgressBtn;
    @FXML private Button browseExercisesBtn;

    private User currentUser;
    private UserService userService;
    private WorkoutService workoutService;
    private BodyStatsService bodyStatsService;
    private Object parentController; // Parent controller referansÄ±

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            try {
                initializeServices();
                setupMotivationalTips();
                // KullanÄ±cÄ± bilgisi geldiÄŸinde veriler yÃ¼klenecek
            } catch (Exception e) {
                System.err.println("Dashboard content initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void initializeServices() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        this.userService = serviceManager.getUserService();
        this.workoutService = serviceManager.getWorkoutService();
        this.bodyStatsService = serviceManager.getBodyStatsService();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        Platform.runLater(() -> {
            loadUserData();
            loadStatistics();
            loadRecentWorkouts();
            setupProgressChart();
        });
    }

    // Parent controller'Ä± set etmek iÃ§in
    public void setParentController(Object parent) {
        this.parentController = parent;
    }

    // Parent controller'Ä± almak iÃ§in
    public Object getParent() {
        return this.parentController;
    }

    private void loadUserData() {
        if (currentUser != null) {
            String firstName = currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername();
            welcomeLabel.setText("HoÅŸ Geldiniz, " + firstName + "!");
            
            // BMI hesaplama - gerÃ§ek verilerden
            loadBMIData();
        }
    }

    private void loadBMIData() {
        try {
            BodyStats latestStats = bodyStatsService.getLatestBodyStats(currentUser.getId());
            if (latestStats != null) {
                double bmi = latestStats.calculateBMI();
                currentBMILabel.setText(String.format("%.1f", bmi));
            } else {
                currentBMILabel.setText("--");
            }
        } catch (SQLException e) {
            System.err.println("BMI yÃ¼klenirken hata: " + e.getMessage());
            currentBMILabel.setText("--");
        }
    }

    private void setupMotivationalTips() {
        List<String> tips = Arrays.asList(
            "DÃ¼zenli antrenman yapmak, sadece vÃ¼cudunuzu deÄŸil ruhunuzu da gÃ¼Ã§lendirir!",
            "Her gÃ¼n biraz daha gÃ¼Ã§lÃ¼ olmak iÃ§in kÃ¼Ã§Ã¼k adÄ±mlar atÄ±n.",
            "BaÅŸarÄ±, gÃ¼nlÃ¼k alÄ±ÅŸkanlÄ±klarÄ±n toplamÄ±dÄ±r.",
            "VÃ¼cudunuz yapabilir, aklÄ±nÄ±zÄ± ikna etmeniz yeterli!",
            "BugÃ¼n yapmadÄ±ÄŸÄ±nÄ±z antrenmanÄ± yarÄ±n iki katÄ± zorla yaparsÄ±nÄ±z.",
            "Hedeflerinize odaklanÄ±n, sonuÃ§lar kendiliÄŸinden gelecek.",
            "GÃ¼Ã§lÃ¼ olmak sadece fiziksel deÄŸil, mental bir durumdur.",
            "Her antrenman sizi hedefinize bir adÄ±m daha yaklaÅŸtÄ±rÄ±r.",
            "TutarlÄ±lÄ±k mÃ¼kemmellikten daha Ã¶nemlidir.",
            "BugÃ¼n kendiniz iÃ§in yaptÄ±ÄŸÄ±nÄ±z her ÅŸey, yarÄ±nÄ±n size hediyesidir."
        );
        
        Random random = new Random();
        String randomTip = tips.get(random.nextInt(tips.size()));
        dailyTipLabel.setText(randomTip);
    }

    private void loadStatistics() {
        if (currentUser == null) return;
        
        try {
            // Toplam antrenman sayÄ±sÄ±
            WorkoutService.WorkoutStats stats = workoutService.getWorkoutStats();
            totalWorkoutsLabel.setText(String.valueOf(stats.totalWorkouts));
            
            // Bu ayki antrenmanlar
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now();
            int monthlyWorkouts = getWorkoutCountInDateRange(startOfMonth, endOfMonth);
            monthlyWorkoutsLabel.setText(String.valueOf(monthlyWorkouts));
            
            // Hedef ilerleme (basit hesaplama - aylÄ±k hedef 12 antrenman)
            int monthlyGoal = 12;
            double progressPercentage = (monthlyWorkouts * 100.0) / monthlyGoal;
            goalProgressLabel.setText(String.format("%.0f%%", Math.min(progressPercentage, 100)));
            
        } catch (Exception e) {
            System.err.println("Ä°statistikler yÃ¼klenirken hata: " + e.getMessage());
            // Hata durumunda varsayÄ±lan deÄŸerler
            totalWorkoutsLabel.setText("0");
            monthlyWorkoutsLabel.setText("0");
            goalProgressLabel.setText("0%");
        }
    }

    private int getWorkoutCountInDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<UserWorkout> allWorkouts = workoutService.getAllWorkouts();
            return (int) allWorkouts.stream()
                .filter(workout -> workout.getUserId() == currentUser.getId())
                .filter(workout -> workout.getWorkoutDate() != null)
                .filter(workout -> !workout.getWorkoutDate().isBefore(startDate) && 
                                 !workout.getWorkoutDate().isAfter(endDate))
                .filter(workout -> "completed".equals(workout.getStatus()))
                .count();
        } catch (Exception e) {
            System.err.println("Tarih aralÄ±ÄŸÄ± antrenman sayÄ±sÄ± hesaplanÄ±rken hata: " + e.getMessage());
            return 0;
        }
    }

    private void loadRecentWorkouts() {
        if (currentUser == null) return;
        
        try {
            List<UserWorkout> recentWorkouts = workoutService.getWorkoutHistory(5);
            recentWorkoutsList.getItems().clear();
            
            if (recentWorkouts.isEmpty()) {
                // Placeholder zaten FXML'de tanÄ±mlÄ±
                return;
            }
            
            for (UserWorkout workout : recentWorkouts) {
                String workoutText = formatWorkoutForList(workout);
                recentWorkoutsList.getItems().add(workoutText);
            }
            
        } catch (Exception e) {
            System.err.println("Son antrenmanlar yÃ¼klenirken hata: " + e.getMessage());
        }
    }

    private String formatWorkoutForList(UserWorkout workout) {
        String icon = getWorkoutIcon(workout);
        String status = getWorkoutStatusText(workout);
        String date = workout.getWorkoutDate() != null ? 
            workout.getWorkoutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : 
            "Tarih yok";
        
        String title = workout.getWorkoutTitle();
        if (title.length() > 30) {
            title = title.substring(0, 27) + "...";
        }
        
        return String.format("%s %s - %s %s", icon, title, date, status);
    }

    private String getWorkoutIcon(UserWorkout workout) {
        if (workout.isRoutineWorkout()) {
            return "ğŸ“‹";
        } else {
            return "ğŸ‹ï¸";
        }
    }

    private String getWorkoutStatusText(UserWorkout workout) {
        switch (workout.getStatus()) {
            case "completed": return "âœ…";
            case "active": return "ğŸ”„";
            case "cancelled": return "âŒ";
            default: return "ğŸ“…";
        }
    }

    private void setupProgressChart() {
        if (currentUser == null) return;
        
        progressChart.setTitle("Son 4 Hafta Antrenman Ä°lerlemesi");
        progressChart.setCreateSymbols(true);
        progressChart.setLegendVisible(false);
        progressChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Antrenman SayÄ±sÄ±");

        try {
            // Son 4 hafta iÃ§in veri hesapla
            LocalDate today = LocalDate.now();
            for (int i = 3; i >= 0; i--) {
                LocalDate weekStart = today.minusWeeks(i).minusDays(today.minusWeeks(i).getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                int weeklyWorkouts = getWorkoutCountInDateRange(weekStart, weekEnd);
                String weekLabel = (i == 0) ? "Bu Hafta" : (i + 1) + ". Hafta Ã–nce";
                
                series.getData().add(new XYChart.Data<>(weekLabel, weeklyWorkouts));
            }
            
        } catch (Exception e) {
            System.err.println("Grafik verileri hesaplanÄ±rken hata: " + e.getMessage());
            // Hata durumunda demo veriler
            series.getData().add(new XYChart.Data<>("3. Hafta Ã–nce", 2));
            series.getData().add(new XYChart.Data<>("2. Hafta Ã–nce", 3));
            series.getData().add(new XYChart.Data<>("1. Hafta Ã–nce", 1));
            series.getData().add(new XYChart.Data<>("Bu Hafta", 4));
        }

        progressChart.getData().add(series);
    }

    // HÄ±zlÄ± eylem metodlarÄ±
    @FXML
    private void handleStartWorkout() {
        System.out.println("Antrenman baÅŸlatÄ±lÄ±yor...");
        try {
            // Parent controller'dan content deÄŸiÅŸtirme metodunu Ã§aÄŸÄ±r
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.loadWorkoutStartContent();
            }
        } catch (Exception e) {
            System.err.println("Antrenman baÅŸlatma sayfasÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showInfo("Hata", "Antrenman baÅŸlatma sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    @FXML
    private void handleTrackWorkout() {
        System.out.println("Antrenman takibi gÃ¶rÃ¼ntÃ¼leniyor...");
        try {
            // Parent controller'dan content deÄŸiÅŸtirme metodunu Ã§aÄŸÄ±r
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.loadWorkoutTrackingContent();
            }
        } catch (Exception e) {
            System.err.println("Antrenman takip sayfasÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showInfo("Hata", "Antrenman takip sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    @FXML
    private void handleAddBodyStats() {
        System.out.println("VÃ¼cut Ã¶lÃ§Ã¼mÃ¼ ekleniyor...");
        try {
            // Parent controller'dan content deÄŸiÅŸtirme metodunu Ã§aÄŸÄ±r
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.handleBodyStats();
            }
        } catch (Exception e) {
            System.err.println("VÃ¼cut Ã¶lÃ§Ã¼mÃ¼ sayfasÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showInfo("Hata", "VÃ¼cut Ã¶lÃ§Ã¼mÃ¼ sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    @FXML
    private void handleViewProgress() {
        System.out.println("Ä°lerleme gÃ¶rÃ¼ntÃ¼leniyor...");
        try {
            // Parent controller'dan content deÄŸiÅŸtirme metodunu Ã§aÄŸÄ±r
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.handleProgress();
            }
        } catch (Exception e) {
            System.err.println("Ä°lerleme sayfasÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showInfo("Hata", "Ä°lerleme sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    @FXML
    private void handleBrowseExercises() {
        System.out.println("Egzersizler keÅŸfediliyor...");
        try {
            // Parent controller'dan content deÄŸiÅŸtirme metodunu Ã§aÄŸÄ±r
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.handleExerciseLibrary();
            }
        } catch (Exception e) {
            System.err.println("Egzersiz kÃ¼tÃ¼phanesi sayfasÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showInfo("Hata", "Egzersiz kÃ¼tÃ¼phanesi sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 



