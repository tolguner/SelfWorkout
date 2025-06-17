package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import com.example.selfworkout.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SystemReportsController implements Initializable {

    // Labels for stats
    @FXML private Label totalUsersLabel;
    @FXML private Label totalExercisesLabel;
    @FXML private Label totalWorkoutsLabel;
    
    // Table for user reports
    @FXML private TableView<UserReportData> userReportsTable;
    @FXML private TableColumn<UserReportData, String> userNameColumn;
    @FXML private TableColumn<UserReportData, Integer> workoutCountColumn;
    @FXML private TableColumn<UserReportData, String> lastActivityColumn;
    @FXML private TableColumn<UserReportData, String> totalTimeColumn;
    
    // Buttons
    @FXML private Button refreshReportsButton;
    
    // Services
    private ServiceManager serviceManager;
    private UserService userService;
    private ExerciseService exerciseService;
    private WorkoutService workoutService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupTable();
        loadReports();
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            userService = serviceManager.getUserService();
            exerciseService = serviceManager.getExerciseService();
            workoutService = serviceManager.getWorkoutService();
        } catch (Exception e) {
            System.err.println("âŒ Service initialization hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Servisler baÅŸlatÄ±lamadÄ±: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        userNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().userName));
        workoutCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().workoutCount).asObject());
        lastActivityColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().lastActivity));
        totalTimeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().totalTime));
    }
    
    private void loadReports() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Quick stats
                List<User> allUsers = userService.getAllUsers();
                int totalUsers = allUsers.size();
                int totalExercises = exerciseService.getAllExercises().size();
                int totalWorkouts = 0; // workoutService.getAllWorkouts().size() when implemented
                
                Platform.runLater(() -> {
                    totalUsersLabel.setText(String.valueOf(totalUsers));
                    totalExercisesLabel.setText(String.valueOf(totalExercises));
                    totalWorkoutsLabel.setText(String.valueOf(totalWorkouts));
                });
                
                // User reports (simplified)
                Platform.runLater(() -> {
                    userReportsTable.getItems().clear();
                    for (User user : allUsers) {
                        UserReportData reportData = new UserReportData(
                            user.getUsername(),
                            0, // workout count placeholder
                            "Bilinmiyor", // last activity placeholder
                            "0 dk" // total time placeholder
                        );
                        userReportsTable.getItems().add(reportData);
                    }
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Raporlar yÃ¼klenirken hata oluÅŸtu: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleRefreshReports() {
        loadReports();
    }
    
    // Navigation methods
    @FXML
    private void handleDashboardNav() {
        try {
            Stage stage = (Stage) refreshReportsButton.getScene().getWindow();
            SceneManager.getInstance().switchToAdminDashboard(stage, getCurrentUser());
        } catch (Exception e) {
            showAlert("Hata", "Dashboard'a geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUserManagement() {
        try {
            Stage stage = (Stage) refreshReportsButton.getScene().getWindow();
            SceneManager.getInstance().switchToAdminDashboard(stage, getCurrentUser());
        } catch (Exception e) {
            showAlert("Hata", "KullanÄ±cÄ± yÃ¶netimine geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExerciseManagement() {
        try {
            Stage stage = (Stage) refreshReportsButton.getScene().getWindow();
            SceneManager.getInstance().switchToExerciseManagement(stage, getCurrentUser());
        } catch (Exception e) {
            showAlert("Hata", "Egzersiz yÃ¶netimine geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDatabaseManagement() {
        showAlert("Bilgi", "VeritabanÄ± yÃ¶netimi Ã¶zelliÄŸi yakÄ±nda eklenecek!");
    }
    
    @FXML
    private void handleSystemSettings() {
        showAlert("Bilgi", "Sistem ayarlarÄ± Ã¶zelliÄŸi yakÄ±nda eklenecek!");
    }
    
    @FXML
    private void handleSwitchToUser() {
        try {
            Stage stage = (Stage) refreshReportsButton.getScene().getWindow();
            SceneManager.getInstance().switchToMainDashboard(stage, getCurrentUser());
        } catch (Exception e) {
            showAlert("Hata", "KullanÄ±cÄ± paneline geÃ§iÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            serviceManager.getAuthenticationService().logout();
            Stage stage = (Stage) refreshReportsButton.getScene().getWindow();
            SceneManager.getInstance().logout(stage);
        } catch (Exception e) {
            showAlert("Hata", "Ã‡Ä±kÄ±ÅŸ sÄ±rasÄ±nda hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        return serviceManager.getAuthenticationService().getCurrentUser();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Data class for user reports
    public static class UserReportData {
        public final String userName;
        public final int workoutCount;
        public final String lastActivity;
        public final String totalTime;
        
        public UserReportData(String userName, int workoutCount, String lastActivity, String totalTime) {
            this.userName = userName;
            this.workoutCount = workoutCount;
            this.lastActivity = lastActivity;
            this.totalTime = totalTime;
        }
    }
} 



