package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Log;
import com.example.selfworkout.service.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardOverviewSimpleController implements Initializable {

    // Header Labels
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    
    // Stats Cards
    @FXML private Label totalUsersCard;
    @FXML private Label totalExercisesCard;
    @FXML private Label totalEquipmentCard;
    
    // Recent Activities
    @FXML private VBox recentActivitiesContainer;
    
    // Quick Action Buttons (dummy - these will be handled by parent controller)
    @FXML private Button userManagementBtn;
    @FXML private Button exerciseManagementBtn;
    @FXML private Button systemReportsBtn;
    
    // Services
    private ServiceManager serviceManager;
    private UserService userService;
    private ExerciseService exerciseService;
    private LogService logService;
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("ğŸ”„ DashboardOverviewSimple initialize baÅŸlÄ±yor");
            
            initializeServices();
            setupWelcomeMessage();
            loadDashboardData();
            
            System.out.println("âœ… DashboardOverviewSimple initialize tamamlandÄ±");
        } catch (Exception e) {
            System.err.println("âŒ DashboardOverviewSimple initialize hatasÄ±: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            userService = serviceManager.getUserService();
            exerciseService = serviceManager.getExerciseService();
            logService = serviceManager.getLogService();
            currentUser = serviceManager.getAuthenticationService().getCurrentUser();
        } catch (Exception e) {
            System.err.println("âŒ DashboardOverviewSimple Service initialization hatasÄ±: " + e.getMessage());
        }
    }
    
    private void setupWelcomeMessage() {
        try {
            if (currentUser != null && welcomeLabel != null) {
                welcomeLabel.setText("ğŸ¯ HoÅŸ Geldiniz, " + currentUser.getFullName() + "!");
            }
            
            // Set current date
            if (dateLabel != null) {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("tr", "TR"));
                dateLabel.setText("BugÃ¼n: " + now.format(formatter));
            }
        } catch (Exception e) {
            System.err.println("âŒ Welcome message setup hatasÄ±: " + e.getMessage());
        }
    }
    
    private void loadDashboardData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Load users data
                    List<User> allUsers = userService.getAllUsers();
                    int totalUsers = allUsers.size();
                    
                    // Load exercises data
                    int totalExercises = exerciseService.getAllExercises().size();
                    
                    // Load equipment data
                    int totalEquipment = serviceManager.getEquipmentService().getAllEquipment().size();
                    
                    Platform.runLater(() -> {
                        if (totalUsersCard != null) totalUsersCard.setText(String.valueOf(totalUsers));
                        if (totalExercisesCard != null) totalExercisesCard.setText(String.valueOf(totalExercises));
                        if (totalEquipmentCard != null) totalEquipmentCard.setText(String.valueOf(totalEquipment));
                        

                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Dashboard verisi yÃ¼klenirken hata: " + e.getMessage());
                        // Show default values on error
                        if (totalUsersCard != null) totalUsersCard.setText("0");
                        if (totalExercisesCard != null) totalExercisesCard.setText("0");
                        if (totalEquipmentCard != null) totalEquipmentCard.setText("0");
                    });
                }
                
                return null;
            }
        };
        
        new Thread(task).start();
        
        // Load recent activities after data is loaded
        Platform.runLater(() -> loadRecentActivities());
    }
    
    // Dummy event handlers (actual handling done by parent controller)
    @FXML
    private void handleUserManagement() {
        showAlert("Bilgi", "ğŸ‘¤ KullanÄ±cÄ± YÃ¶netimi iÃ§in sol menÃ¼yÃ¼ kullanÄ±n");
    }
    
    @FXML
    private void handleExerciseManagement() {
        showAlert("Bilgi", "ğŸ‹ï¸ Egzersiz YÃ¶netimi iÃ§in sol menÃ¼yÃ¼ kullanÄ±n");
    }
    
    @FXML
    private void handleSystemReports() {
        showAlert("Bilgi", "ğŸ“Š Sistem RaporlarÄ± iÃ§in sol menÃ¼yÃ¼ kullanÄ±n");
    }
    
    @FXML
    private void handleViewAllActivities() {
        showAlert("Bilgi", "ğŸ“‹ Aktivite GeÃ§miÅŸi SayfasÄ±\n\nâœ¨ TÃ¼m sistem aktiviteleri artÄ±k gÃ¶rÃ¼ntÃ¼lenebilir!\n\nğŸ¯ Ã–zellikler:\nâ€¢ DetaylÄ± aktivite loglarÄ±\nâ€¢ Tarih ve eylem filtreleri\nâ€¢ KullanÄ±cÄ± bazÄ±nda arama\nâ€¢ Ä°statistik gÃ¶rÃ¼ntÃ¼leme\nâ€¢ Sayfalama desteÄŸi\n\nâ¡ï¸ Sol menÃ¼den 'ğŸ“‹ Aktivite GeÃ§miÅŸi' sekmesini kullanÄ±n!");
    }
    
    /**
     * Current user'Ä± set et
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("âœ… DashboardOverviewSimple: Current user set to " + (user != null ? user.getUsername() : "null"));
        
        // Update welcome message if user is set after initialization
        if (user != null && welcomeLabel != null) {
            Platform.runLater(() -> {
                welcomeLabel.setText("ğŸ¯ HoÅŸ Geldiniz, " + user.getFullName() + "!");
            });
        }
    }
    
    private void loadRecentActivities() {
        if (recentActivitiesContainer == null) return;
        
        try {
            // Clear existing content
            recentActivitiesContainer.getChildren().clear();
            
            // GerÃ§ek log kayÄ±tlarÄ±nÄ± yÃ¼kle
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        // Son 5 log kaydÄ±nÄ± getir
                        List<Log> recentLogs = logService.getRecentLogs(5);
                        
                        if (recentLogs.isEmpty()) {
                            Platform.runLater(() -> {
                                // Log yoksa bilgi mesajÄ± gÃ¶ster
                                addInfoMessage("HenÃ¼z log kaydÄ± bulunmuyor", "Sistem kullanÄ±ldÄ±kÃ§a aktivite kayÄ±tlarÄ± burada gÃ¶rÃ¼necek");
                            });
                            return null;
                        }
                        
                        // UI thread'de log kayÄ±tlarÄ±nÄ± ekle
                        Platform.runLater(() -> {
                            for (Log log : recentLogs) {
                                String icon = getIconForAction(log.getAction());
                                String status = getStatusForAction(log.getAction());
                                String timeAgo = formatTimeAgo(log.getTimestamp());
                                
                                addActivityItem(icon, log.getAction(), log.getDescription(), timeAgo, status);
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("âŒ Log yÃ¼kleme hatasÄ±: " + e.getMessage());
                        Platform.runLater(() -> {
                            addInfoMessage("Log kayÄ±tlarÄ± yÃ¼klenemedi", "VeritabanÄ± baÄŸlantÄ±sÄ± kontrol ediliyor...");
                        });
                    }
                    return null;
                }
            };
            
            new Thread(task).start();
            
        } catch (Exception e) {
            System.err.println("âŒ Recent activities load hatasÄ±: " + e.getMessage());
            addInfoMessage("Hata", "Aktivite kayÄ±tlarÄ± yÃ¼klenirken bir sorun oluÅŸtu");
        }
    }
    
    private String getIconForAction(String action) {
        if (action == null) return "â„¹ï¸";
        
        if (action.contains("LOGIN") || action.contains("USER")) return "ğŸ‘¤";
        if (action.contains("SYSTEM") || action.contains("INIT")) return "ğŸ”„";
        if (action.contains("ERROR")) return "âŒ";
        if (action.contains("WARNING")) return "âš ï¸";
        if (action.contains("EXERCISE") || action.contains("WORKOUT")) return "ğŸ‹ï¸";
        if (action.contains("STATS") || action.contains("REPORT")) return "ğŸ“Š";
        
        return "â„¹ï¸";
    }
    
    private String getStatusForAction(String action) {
        if (action == null) return "â„¹ï¸";
        
        if (action.contains("ERROR")) return "âŒ";
        if (action.contains("WARNING")) return "âš ï¸";
        if (action.contains("INFO")) return "â„¹ï¸";
        
        return "âœ…";
    }
    
    private String formatTimeAgo(LocalDateTime timestamp) {
        if (timestamp == null) return "Bilinmeyen zaman";
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(timestamp, now);
        
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "Az Ã¶nce";
        } else if (seconds < 3600) {
            return (seconds / 60) + " dakika Ã¶nce";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " saat Ã¶nce";
        } else {
            return (seconds / 86400) + " gÃ¼n Ã¶nce";
        }
    }
    
    private void addActivityItem(String icon, String title, String description, String time, String status) {
        try {
            HBox activityItem = new HBox(20.0);
            activityItem.setAlignment(Pos.CENTER_LEFT);
            activityItem.setStyle("-fx-padding: 16px 20px; -fx-background-color: #f8f9fa; -fx-background-radius: 12px;");
            
            // Icon
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 20px;");
            
            // Content
            VBox contentBox = new VBox(4.0);
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 15px; -fx-text-fill: #2c3e50; -fx-font-weight: 600;");
            
            Label descLabel = new Label(description + " - " + time);
            descLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            
            contentBox.getChildren().addAll(titleLabel, descLabel);
            
            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Status
            Label statusLabel = new Label(status);
            statusLabel.setStyle("-fx-font-size: 18px;");
            
            activityItem.getChildren().addAll(iconLabel, contentBox, spacer, statusLabel);
            recentActivitiesContainer.getChildren().add(activityItem);
            
        } catch (Exception e) {
            System.err.println("âŒ Activity item ekleme hatasÄ±: " + e.getMessage());
        }
    }

    private void addInfoMessage(String title, String message) {
        try {
            HBox infoItem = new HBox(20.0);
            infoItem.setAlignment(Pos.CENTER_LEFT);
            infoItem.setStyle("-fx-padding: 16px 20px; -fx-background-color: #e3f2fd; -fx-background-radius: 12px;");
            
            // Icon
            Label iconLabel = new Label("â„¹ï¸");
            iconLabel.setStyle("-fx-font-size: 20px;");
            
            // Content
            VBox contentBox = new VBox(4.0);
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 15px; -fx-text-fill: #1976d2; -fx-font-weight: 600;");
            
            Label descLabel = new Label(message);
            descLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            
            contentBox.getChildren().addAll(titleLabel, descLabel);
            
            infoItem.getChildren().addAll(iconLabel, contentBox);
            recentActivitiesContainer.getChildren().add(infoItem);
            
        } catch (Exception e) {
            System.err.println("âŒ Info message ekleme hatasÄ±: " + e.getMessage());
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



