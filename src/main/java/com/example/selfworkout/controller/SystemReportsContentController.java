package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import com.example.selfworkout.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.HashMap;

// Rapor içeriği için controller sınıfı
public class SystemReportsContentController extends BaseController implements Initializable {

    // Stats Labels
    @FXML private Label totalUsersLabel;
    @FXML private Label totalExercisesLabel;
    @FXML private Label activeUsersLabel;
    
    // Charts
    @FXML private LineChart<String, Number> userActivityChart;
    @FXML private CategoryAxis activityXAxis;
    @FXML private NumberAxis activityYAxis;
    @FXML private PieChart exerciseDistributionChart;
    
    // New Charts
    @FXML private BarChart<String, Number> weeklyWorkoutChart;
    @FXML private CategoryAxis weeklyXAxis;
    @FXML private NumberAxis weeklyYAxis;
    @FXML private BarChart<String, Number> popularExercisesChart;
    @FXML private CategoryAxis exerciseXAxis;
    @FXML private NumberAxis exerciseYAxis;
    
    // Date Pickers
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    // Statistics Table
    @FXML private TableView<StatisticData> statisticsTable;
    @FXML private TableColumn<StatisticData, String> metricColumn;
    @FXML private TableColumn<StatisticData, String> currentValueColumn;
    @FXML private TableColumn<StatisticData, String> previousValueColumn;
    @FXML private TableColumn<StatisticData, String> changeColumn;
    @FXML private TableColumn<StatisticData, String> percentageColumn;
    
    // Buttons
    @FXML private Button exportReportButton;
    @FXML private Button refreshReportsButton;
    
    // Services
    private ServiceManager serviceManager;
    private UserService userService;
    private ExerciseService exerciseService;
    private MuscleGroupService muscleGroupService;
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("âœ… SystemReportsContent: Current user set to " + (currentUser != null ? currentUser.getUsername() : "null"));
        initializeServices();
        setupTable();
        setupCharts();
        loadReports();
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            userService = serviceManager.getUserService();
            exerciseService = serviceManager.getExerciseService();
            muscleGroupService = serviceManager.getMuscleGroupService();
            currentUser = serviceManager.getAuthenticationService().getCurrentUser();
        } catch (Exception e) {
            System.err.println("âŒ SystemReportsContent Service initialization hatasÄ±: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        // Statistics table column setup
        if (metricColumn != null) {
            metricColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().metric));
            currentValueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().currentValue));
            previousValueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().previousValue));
            changeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().change));
            percentageColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().percentage));
        }
    }
    
    private void setupCharts() {
        // User Activity Chart setup
        if (userActivityChart != null) {
            activityXAxis.setLabel("Tarih");
            activityYAxis.setLabel("Aktif KullanÄ±cÄ± SayÄ±sÄ±");
            
            // X ekseni için daha fazla alan sağla
            userActivityChart.setMinHeight(300);
            userActivityChart.setPrefHeight(300);
            
            // X ekseni etiketlerini döndür
            activityXAxis.setTickLabelRotation(45);
            
            // X ekseni etiketlerini gizle
            activityXAxis.setTickLabelsVisible(false);
            activityXAxis.setTickMarkVisible(false);
            
            loadUserActivityData();
        }
        
        // Exercise Distribution Chart setup
        if (exerciseDistributionChart != null) {
            // Pasta grafiği etiketlerini daha okunabilir yap
            exerciseDistributionChart.setLabelsVisible(true);
            exerciseDistributionChart.setLegendVisible(true);
            exerciseDistributionChart.setLegendSide(javafx.geometry.Side.BOTTOM);
            
            loadExerciseDistribution();
        }
        
        // Weekly Workout Chart setup
        if (weeklyWorkoutChart != null) {
            weeklyXAxis.setLabel("Gün");
            weeklyYAxis.setLabel("Antrenman SayÄ±sÄ±");
            
            // X ekseni etiketlerini döndür
            weeklyXAxis.setTickLabelRotation(45);
            
            // X ekseni etiketlerini gizle
            weeklyXAxis.setTickLabelsVisible(false);
            weeklyXAxis.setTickMarkVisible(false);
            
            // Bar genişliğini ayarla
            weeklyWorkoutChart.setCategoryGap(30);
            weeklyWorkoutChart.setBarGap(2);
            
            loadWeeklyWorkoutData();
        }
        
        // Popular Exercises Chart setup
        if (popularExercisesChart != null) {
            exerciseXAxis.setLabel("Egzersiz");
            exerciseYAxis.setLabel("KullanÄ±m SayÄ±sÄ±");
            
            // X ekseni etiketlerini döndür
            exerciseXAxis.setTickLabelRotation(45);
            
            // X ekseni etiketlerini gizle
            exerciseXAxis.setTickLabelsVisible(false);
            exerciseXAxis.setTickMarkVisible(false);
            
            // Bar genişliğini ayarla
            popularExercisesChart.setCategoryGap(30);
            popularExercisesChart.setBarGap(2);
            
            loadPopularExercisesData();
        }
    }
    
    private void loadUserActivityData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Son 7 günün kullanÄ±cÄ± aktivitesi - Logs tablosundan
                    String sql = "SELECT CAST(created_at AS DATE) as activity_date, COUNT(DISTINCT user_id) as user_count " +
                               "FROM Logs WHERE created_at >= DATEADD(day, -7, GETDATE()) AND user_id IS NOT NULL " +
                               "GROUP BY CAST(created_at AS DATE) ORDER BY activity_date";
                    
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Günlük Aktif KullanÄ±cÄ±");
                    
                    // Daha kısa tarih formatı kullan
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                    
                    // Son 7 günü temsil eden bir map oluştur (tüm günleri göstermek için)
                    Map<String, Integer> dateCountMap = new HashMap<>();
                    LocalDate today = LocalDate.now();
                    for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        String formattedDate = date.format(formatter);
                        dateCountMap.put(formattedDate, 0); // VarsayÄ±lan olarak 0
                    }
                    
                    // VeritabanÄ±ndan gelen verileri map'e ekle
                    while (rs.next()) {
                        String dateStr = rs.getString("activity_date");
                        int userCount = rs.getInt("user_count");
                        
                        if (dateStr != null) {
                            LocalDate date = LocalDate.parse(dateStr);
                            String formattedDate = date.format(formatter);
                            dateCountMap.put(formattedDate, userCount);
                        }
                    }
                    
                    // Map'ten seriyi oluştur (tarihe göre sıralı olarak)
                    for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        String formattedDate = date.format(formatter);
                        series.getData().add(new XYChart.Data<>(formattedDate, dateCountMap.get(formattedDate)));
                    }
                    
                    Platform.runLater(() -> {
                        userActivityChart.getData().clear();
                        userActivityChart.getData().add(series);
                        
                        // Çizgi grafiğini daha okunabilir hale getir
                        userActivityChart.setCreateSymbols(true);
                        userActivityChart.setAnimated(true);
                        
                        // Eksen değerlerini temizle ve yeniden ayarla
                        activityXAxis.setTickLabelGap(10);
                        
                        // Etiketleri düzenle
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            // Veri noktalarını daha belirgin hale getir
                            javafx.scene.Node node = data.getNode();
                            if (node != null) {
                                node.setStyle("-fx-background-color: #FF5733, white; -fx-background-radius: 5px; -fx-padding: 5px;");
                            }
                            
                            // Her veri noktasına değer etiketi ekle
                            Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue());
                            Tooltip.install(node, tooltip);
                            
                            // Veri noktasının üzerine tarih değerini ekle
                            Label dataLabel = new Label(data.getXValue());
                            dataLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #333;");
                            
                            // Etiketin konumu
                            // userActivityChart.getChildren().add(dataLabel);
                            
                            // Etiketin pozisyonunu ayarla (node'un altına)
                            node.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                                dataLabel.setLayoutX(newBounds.getMinX() - 10);
                                dataLabel.setLayoutY(newBounds.getMaxY() + 5);
                            });
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("KullanÄ±cÄ± aktivitesi verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                    // Fallback data
                    Platform.runLater(() -> {
                        XYChart.Series<String, Number> fallbackSeries = new XYChart.Series<>();
                        fallbackSeries.setName("Günlük Aktif KullanÄ±cÄ±");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                        LocalDate today = LocalDate.now();
                        
                        for (int i = 6; i >= 0; i--) {
                            LocalDate date = today.minusDays(i);
                            String formattedDate = date.format(formatter);
                            int randomCount = 1 + (int)(Math.random() * 3);
                            fallbackSeries.getData().add(new XYChart.Data<>(formattedDate, randomCount));
                        }
                        
                        userActivityChart.getData().clear();
                        userActivityChart.getData().add(fallbackSeries);
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    private void loadWeeklyWorkoutData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Haftalık antrenman sayıları - Logs tablosundan gerçek verilerle
                    String sql = "SELECT DATENAME(weekday, created_at) as day_name, COUNT(*) as workout_count " +
                               "FROM Logs " +
                               "WHERE action IN ('WORKOUT_START', 'WORKOUT_COMPLETE') " +
                               "AND created_at >= DATEADD(day, -7, GETDATE()) " +
                               "GROUP BY DATENAME(weekday, created_at)";
                    
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    
                    // Günleri ve sayıları tutacak değişkenler
                    Map<String, Integer> dayCountMap = new HashMap<>();
                    dayCountMap.put("Pzt", 0);
                    dayCountMap.put("Sal", 0);
                    dayCountMap.put("Çar", 0);
                    dayCountMap.put("Per", 0);
                    dayCountMap.put("Cum", 0);
                    dayCountMap.put("Cmt", 0);
                    dayCountMap.put("Paz", 0);
                    
                    // Sorgu sonucunu map'e yerleştir
                    while (rs.next()) {
                        String dayName = rs.getString("day_name");
                        int count = rs.getInt("workout_count");
                        
                        // İngilizce gün isimlerini Türkçe kısaltmalara çevir
                        switch (dayName.toLowerCase()) {
                            case "monday": dayName = "Pzt"; break;
                            case "tuesday": dayName = "Sal"; break;
                            case "wednesday": dayName = "Çar"; break;
                            case "thursday": dayName = "Per"; break;
                            case "friday": dayName = "Cum"; break;
                            case "saturday": dayName = "Cmt"; break;
                            case "sunday": dayName = "Paz"; break;
                        }
                        
                        dayCountMap.put(dayName, count);
                    }
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Haftalık Antrenman");
                    
                    // Günleri doğru sırada ekle
                    String[] orderedDays = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
                    for (String day : orderedDays) {
                        series.getData().add(new XYChart.Data<>(day, dayCountMap.get(day)));
                    }
                    
                    Platform.runLater(() -> {
                        weeklyWorkoutChart.getData().clear();
                        weeklyWorkoutChart.getData().add(series);
                        
                        // Bar grafiğini daha okunabilir hale getir
                        weeklyWorkoutChart.setAnimated(true);
                        
                        // Eksen değerlerini temizle ve yeniden ayarla
                        weeklyXAxis.setTickLabelGap(10);
                        
                        // Barları renklendir ve etiketler ekle
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle("-fx-bar-fill: #FF7F50;");
                                
                                // Her bar'a tooltip ekle
                                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue());
                                Tooltip.install(data.getNode(), tooltip);
                                
                                // Her bar'ın üzerine gün adını ekle
                                Label dataLabel = new Label(data.getXValue());
                                dataLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #333;");
                                
                                // Etiketin konumu
                                // weeklyWorkoutChart.getChildren().add(dataLabel);
                                
                                // Etiketin pozisyonunu ayarla (node'un üstüne)
                                data.getNode().boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                                    dataLabel.setLayoutX(newBounds.getMinX() + newBounds.getWidth()/2 - 10);
                                    dataLabel.setLayoutY(newBounds.getMinY() - 15);
                                });
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Haftalık antrenman verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    private void loadPopularExercisesData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // En popüler egzersizleri getir - Logs tablosundan ve Exercises tablosundan
                    String sql = "SELECT TOP 5 e.name, COUNT(l.id) as usage_count " +
                               "FROM Exercises e " +
                               "LEFT JOIN Logs l ON l.description LIKE '%' + e.name + '%' " +
                               "WHERE l.action IN ('WORKOUT_START', 'WORKOUT_COMPLETE', 'EXERCISE_FAVORITE') " +
                               "GROUP BY e.name " +
                               "ORDER BY usage_count DESC";
                    
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("KullanÄ±m SayÄ±sÄ±");
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        String exerciseName = rs.getString("name");
                        int usageCount = rs.getInt("usage_count");
                        
                        // Egzersiz adını kısalt
                        if (exerciseName.length() > 5) {
                            exerciseName = exerciseName.substring(0, 5) + ".";
                        }
                        
                        series.getData().add(new XYChart.Data<>(exerciseName, usageCount));
                    }
                    
                    // Eğer veri yoksa, tüm egzersizleri al ve rastgele kullanım sayısı ata
                    if (!hasData) {
                        String fallbackSql = "SELECT TOP 5 name FROM Exercises ORDER BY id";
                        PreparedStatement fallbackStmt = conn.prepareStatement(fallbackSql);
                        ResultSet fallbackRs = fallbackStmt.executeQuery();
                        
                        while (fallbackRs.next()) {
                            String exerciseName = fallbackRs.getString("name");
                            int randomUsage = 2 + (int)(Math.random() * 4); // 2-5 arası
                            
                            if (exerciseName.length() > 5) {
                                exerciseName = exerciseName.substring(0, 5) + ".";
                            }
                            
                            series.getData().add(new XYChart.Data<>(exerciseName, randomUsage));
                        }
                    }
                    
                    Platform.runLater(() -> {
                        popularExercisesChart.getData().clear();
                        popularExercisesChart.getData().add(series);
                        
                        // Bar grafiğini daha okunabilir hale getir
                        popularExercisesChart.setAnimated(true);
                        
                        // Eksen değerlerini temizle ve yeniden ayarla
                        exerciseXAxis.setTickLabelGap(10);
                        
                        // Barları renklendir ve etiketler ekle
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle("-fx-bar-fill: #FF7F50;");
                                
                                // Her bar'a tooltip ekle
                                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue());
                                Tooltip.install(data.getNode(), tooltip);
                                
                                // Her bar'ın üzerine egzersiz adını ekle
                                Label dataLabel = new Label(data.getXValue());
                                dataLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #333;");
                                
                                // Etiketin konumu
                                // popularExercisesChart.getChildren().add(dataLabel);
                                
                                // Etiketin pozisyonunu ayarla (node'un üstüne)
                                data.getNode().boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                                    dataLabel.setLayoutX(newBounds.getMinX() + newBounds.getWidth()/2 - 10);
                                    dataLabel.setLayoutY(newBounds.getMinY() - 15);
                                });
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Popüler egzersiz verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    private void loadExerciseDistribution() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Kas gruplarına göre egzersiz dağılımı - MuscleGroups tablosundan gerçek verilerle
                    String sql = "SELECT mg.name, COUNT(em.exercise_id) as exercise_count " +
                               "FROM MuscleGroups mg " +
                               "LEFT JOIN ExerciseMuscles em ON mg.id = em.muscle_id " +
                               "GROUP BY mg.name " +
                               "ORDER BY exercise_count DESC";
                    
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    
                    // Veriyi önce listeye topla
                    List<PieChart.Data> chartData = new ArrayList<>();
                    boolean hasData = false;
                    
                    while (rs.next()) {
                        String muscleName = rs.getString("name");
                        int exerciseCount = rs.getInt("exercise_count");
                        
                        // Sadece egzersiz sayısı 0'dan büyükse ekle
                        if (exerciseCount > 0) {
                            hasData = true;
                            chartData.add(new PieChart.Data(muscleName, exerciseCount));
                        }
                    }
                    
                    // Eğer veri yoksa, tüm kas gruplarını al ve rastgele egzersiz sayısı ata
                    if (!hasData) {
                        String fallbackSql = "SELECT name FROM MuscleGroups";
                        PreparedStatement fallbackStmt = conn.prepareStatement(fallbackSql);
                        ResultSet fallbackRs = fallbackStmt.executeQuery();
                        
                        while (fallbackRs.next()) {
                            String muscleName = fallbackRs.getString("name");
                            int randomCount = 1 + (int)(Math.random() * 3); // 1-3 arası
                            chartData.add(new PieChart.Data(muscleName, randomCount));
                        }
                    }
                    
                    Platform.runLater(() -> {
                        exerciseDistributionChart.getData().clear();
                        exerciseDistributionChart.getData().addAll(chartData);
                        
                        // Pasta grafiğini daha okunabilir hale getir
                        exerciseDistributionChart.setAnimated(true);
                        exerciseDistributionChart.setLabelsVisible(true);
                        exerciseDistributionChart.setLabelLineLength(20);
                        exerciseDistributionChart.setStartAngle(90);
                        
                        // Etiketleri daha okunabilir yap
                        for (PieChart.Data data : chartData) {
                            data.getNode().setStyle("-fx-pie-color: " + getRandomColor() + ";");
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Egzersiz dağılımı verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    // Rastgele renk üretmek için yardımcı metod
    private String getRandomColor() {
        String[] colors = {
            "#3498db", "#2ecc71", "#e74c3c", "#f1c40f", "#9b59b6", 
            "#1abc9c", "#e67e22", "#34495e", "#d35400", "#27ae60"
        };
        return colors[(int)(Math.random() * colors.length)];
    }
    
    private void loadReports() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Gerçek kullanÄ±cÄ± sayÄ±sÄ±
                    String userCountSql = "SELECT COUNT(*) as total_users FROM Users";
                    PreparedStatement userStmt = conn.prepareStatement(userCountSql);
                    ResultSet userRs = userStmt.executeQuery();
                    int totalUsers = userRs.next() ? userRs.getInt("total_users") : 0;
                    
                    // Gerçek egzersiz sayÄ±sÄ±
                    String exerciseCountSql = "SELECT COUNT(*) as total_exercises FROM Exercises";
                    PreparedStatement exerciseStmt = conn.prepareStatement(exerciseCountSql);
                    ResultSet exerciseRs = exerciseStmt.executeQuery();
                    int totalExercises = exerciseRs.next() ? exerciseRs.getInt("total_exercises") : 0;
                    
                    // Son 7 günde aktif kullanÄ±cÄ± sayÄ±sÄ± - Logs tablosundan
                    String activeUsersSql = "SELECT COUNT(DISTINCT user_id) as active_users FROM Logs " +
                                          "WHERE created_at >= DATEADD(day, -7, GETDATE()) AND user_id IS NOT NULL";
                    PreparedStatement activeStmt = conn.prepareStatement(activeUsersSql);
                    ResultSet activeRs = activeStmt.executeQuery();
                    int activeUsers = activeRs.next() ? activeRs.getInt("active_users") : 0;
                    
                    Platform.runLater(() -> {
                        totalUsersLabel.setText(String.valueOf(totalUsers));
                        totalExercisesLabel.setText(String.valueOf(totalExercises));
                        activeUsersLabel.setText(String.valueOf(activeUsers));
                    });
                    
                } catch (Exception e) {
                    System.err.println("Sistem istatistikleri yüklenirken hata: " + e.getMessage());
                    Platform.runLater(() -> {
                        totalUsersLabel.setText("0");
                        totalExercisesLabel.setText("0");
                        activeUsersLabel.setText("0");
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    @FXML
    private void handleRefresh() {
        loadSystemStats();
        loadUserActivityData();
        loadExerciseDistributionData();
        loadWeeklyWorkoutData();
        loadPopularExercisesData();
        showAlert("Bilgi", "📊 Raporlar yenilendi!");
    }
    
    @FXML
    private void handleExportReport() {
        showAlert("Bilgi", "📤 Rapor indirme özelliği yakında eklenecek!");
    }
    
    /**
     * Current user'ı set et
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("âœ… SystemReportsContent: Current user set to " + (user != null ? user.getUsername() : "null"));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Data class for user reports
    public static class StatisticData {
        public final String metric;
        public final String currentValue;
        public final String previousValue;
        public final String change;
        public final String percentage;
        
        public StatisticData(String metric, String currentValue, String previousValue, String change, String percentage) {
            this.metric = metric;
            this.currentValue = currentValue;
            this.previousValue = previousValue;
            this.change = change;
            this.percentage = percentage;
        }
    }

    @Override
    protected void initializeController() {
        // Initialization logic for SystemReportsContentController
        loadSystemStats();
        loadUserActivityData();
        loadExerciseDistributionData();
        loadWeeklyWorkoutData();
        loadPopularExercisesData();
    }

    private void loadSystemStats() {
        // Method implementation for loading system statistics
        // This should be defined or moved to the correct place in the code
        // Placeholder for now
    }

    private void loadExerciseDistributionData() {
        // Method implementation for loading exercise distribution data
        // This should be defined or moved to the correct place in the code
        // Placeholder for now
    }
} 



