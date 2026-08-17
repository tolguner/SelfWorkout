package com.example.selfworkout.controller;

import com.example.selfworkout.dao.ReportingDAO;
import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import com.example.selfworkout.util.DatabaseConnection; // Doğru import
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.util.Duration; // Tooltip için Duration gerekebilir

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
// BaseController'dan miras almıyorsa, initialize metodu BaseController'daki initializeController'ı çağırmayacaktır.
public class SystemReportsContentController implements Initializable {

    // UI Components
    @FXML private Label totalUsersLabel;
    @FXML private Label totalExercisesLabel;
    @FXML private Label activeUsersLabel;

    @FXML private LineChart<String, Number> userActivityChart;
    @FXML private CategoryAxis activityXAxis;
    @FXML private NumberAxis activityYAxis;
    @FXML private PieChart exerciseDistributionChart;

    @FXML private BarChart<String, Number> weeklyWorkoutChart;
    @FXML private CategoryAxis weeklyXAxis;
    @FXML private NumberAxis weeklyYAxis;
    @FXML private BarChart<String, Number> popularExercisesChart;
    @FXML private CategoryAxis exerciseXAxis;
    @FXML private NumberAxis exerciseYAxis;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<StatisticData> statisticsTable; // FXML'de görünmesine rağmen, Java kodu içinde doldurulmuyor.
    @FXML private TableColumn<StatisticData, String> metricColumn;
    @FXML private TableColumn<StatisticData, String> currentValueColumn;
    @FXML private TableColumn<StatisticData, String> previousValueColumn;
    @FXML private TableColumn<StatisticData, String> changeColumn;
    @FXML private TableColumn<StatisticData, String> percentageColumn;

    @FXML private Button exportReportButton;
    @FXML private Button refreshReportsButton;

    // Services
    private ServiceManager serviceManager;
    private UserService userService;
    private ExerciseService exerciseService;
    private MuscleGroupService muscleGroupService;
    private final ReportingDAO reportingDAO = new ReportingDAO();
    private User currentUser; // Bu kullanıcı, SceneManager tarafından atanır.

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bu metot, FXML yüklenirken JavaFX tarafından otomatik çağrılır.
        System.out.println("✅ SystemReportsContent initialize başlıyor.");

        initializeServices();
        setupTable();
        setupCharts();

        // Tarih seçicileri varsayılan değerleri
        if (endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now());
        }
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now().minusMonths(1)); // Son bir ay varsayılan
        }

        loadReports(); // Tüm raporları ve istatistikleri yükleyen ana metod
        System.out.println("✅ SystemReportsContent initialize tamamlandı.");
    }

    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            userService = serviceManager.getUserService();
            exerciseService = serviceManager.getExerciseService();
            muscleGroupService = serviceManager.getMuscleGroupService();
            // currentUser, SceneManager.loadScene() metoduyla atanır.
            // Burada doğrudan erişim yerine setter metodu beklenmeli.
            // Ancak şu anki yapıda, SceneManager'daki setCurrentUser() metodu zaten bu instance'a atıyor.
            // Bu, BaseController'dan miras almayan controller'lar için SceneManager'da özel cast'leri gerektirir.
        } catch (Exception e) {
            System.err.println("❌ SystemReportsContent Service initialization hatası: " + e.getMessage());
            // Hata durumunda kullanıcıya bilgi verilebilir
            showAlert("Hata", "Servisler başlatılırken bir sorun oluştu: " + e.getMessage());
        }
    }

    private void setupTable() {
        // FXML'de 'statisticsTable' varsa ve kolonlar tanımlıysa setup yapar
        if (statisticsTable != null && metricColumn != null) {
            metricColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().metric));
            currentValueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().currentValue));
            previousValueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().previousValue));
            changeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().change));
            percentageColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().percentage));
            // statisticsTable.setItems(FXCollections.observableArrayList(new StatisticData(...))); // Veriler burada doldurulur
        }
    }

    private void setupCharts() {
        if (userActivityChart != null) {
            userActivityChart.setTitle("Kullanıcı Aktivitesi Trendi");
            // FXML'de zaten tanımlı olan ayarları Java kodunda tekrar etmek yerine
            // FXML'in doğru olduğundan emin olmak daha iyidir.
            // activityXAxis.setLabel("Tarih");
            // activityYAxis.setLabel("Aktif Kullanıcı Sayısı");
            // userActivityChart.setMinHeight(300);
            // userActivityChart.setPrefHeight(300);
            // activityXAxis.setTickLabelRotation(45);
            // activityXAxis.setTickLabelsVisible(false);
            // activityXAxis.setTickMarkVisible(false);
        }

        if (exerciseDistributionChart != null) {
            exerciseDistributionChart.setTitle("Egzersiz Kategorisi Dağılımı");
            // exerciseDistributionChart.setLabelsVisible(true);
            // exerciseDistributionChart.setLegendVisible(true);
            // exerciseDistributionChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        }

        if (weeklyWorkoutChart != null) {
            weeklyWorkoutChart.setTitle("Haftalık Antrenman Sıklığı");
            // weeklyXAxis.setLabel("Gün");
            // weeklyYAxis.setLabel("Antrenman Sayısı");
            // weeklyXAxis.setTickLabelRotation(45);
            // weeklyXAxis.setTickLabelsVisible(false);
            // weeklyXAxis.setTickMarkVisible(false);
            // weeklyWorkoutChart.setCategoryGap(30);
            // weeklyWorkoutChart.setBarGap(2);
        }

        if (popularExercisesChart != null) {
            popularExercisesChart.setTitle("En Popüler Egzersizler");
            // exerciseXAxis.setLabel("Egzersiz");
            // exerciseYAxis.setLabel("Kullanım Sayısı");
            // exerciseXAxis.setTickLabelRotation(45);
            // exerciseXAxis.setTickLabelsVisible(false);
            // exerciseXAxis.setTickMarkVisible(false);
            // popularExercisesChart.setCategoryGap(30);
            // popularExercisesChart.setBarGap(2);
        }
    }

    private void loadUserActivityData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    String sql = "SELECT CAST(created_at AS DATE) as activity_date, COUNT(DISTINCT user_id) as user_count " +
                            "FROM Logs WHERE created_at >= DATEADD(day, -7, GETDATE()) AND user_id IS NOT NULL " +
                            "GROUP BY CAST(created_at AS DATE) ORDER BY activity_date";

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Günlük Aktif Kullanıcı");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

                    Map<String, Integer> dateCountMap = new HashMap<>();
                    LocalDate today = LocalDate.now();
                    for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        String formattedDate = date.format(formatter);
                        dateCountMap.put(formattedDate, 0);
                    }

                    while (rs.next()) {
                        String dateStr = rs.getString("activity_date");
                        int userCount = rs.getInt("user_count");

                        if (dateStr != null) {
                            LocalDate date = LocalDate.parse(dateStr);
                            String formattedDate = date.format(formatter);
                            dateCountMap.put(formattedDate, userCount);
                        }
                    }

                    for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        String formattedDate = date.format(formatter);
                        series.getData().add(new XYChart.Data<>(formattedDate, dateCountMap.get(formattedDate)));
                    }

                    Platform.runLater(() -> {
                        userActivityChart.getData().clear();
                        userActivityChart.getData().add(series);
                        userActivityChart.setCreateSymbols(true);
                        userActivityChart.setAnimated(true);
                        activityXAxis.setTickLabelGap(10);

                        for (XYChart.Data<String, Number> data : series.getData()) {
                            javafx.scene.Node node = data.getNode();
                            if (node != null) {
                                // Daha görünür bir stil
                                node.setStyle("-fx-background-color: #FF5733; -fx-background-radius: 5px; -fx-padding: 5px;");
                                // Tooltip ekle
                                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " aktif kullanıcı");
                                // Gecikmeli gösterim için (daha az yorucu)
                                Tooltip.install(node, tooltip);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Kullanıcı aktivitesi verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        // Hata durumunda örnek veriler yükle (daha önceki örnek veriye benzer)
                        XYChart.Series<String, Number> fallbackSeries = new XYChart.Series<>();
                        fallbackSeries.setName("Günlük Aktif Kullanıcı (Demo)");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                        LocalDate today = LocalDate.now();

                        for (int i = 6; i >= 0; i--) {
                            LocalDate date = today.minusDays(i);
                            String formattedDate = date.format(formatter);
                            int randomCount = 1 + (int)(Math.random() * 3); // 1-3 arasında rastgele
                            fallbackSeries.getData().add(new XYChart.Data<>(formattedDate, randomCount));
                        }
                        userActivityChart.getData().clear();
                        userActivityChart.getData().add(fallbackSeries);
                        showAlert("Uyarı", "Kullanıcı aktivite grafiği yüklenemedi. Demo veriler gösteriliyor.");
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
                // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    String sql = "SELECT DATENAME(weekday, created_at) as day_name, COUNT(*) as workout_count " +
                            "FROM Logs " +
                            "WHERE action IN ('WORKOUT_START', 'WORKOUT_COMPLETE') " +
                            "AND created_at >= DATEADD(day, -7, GETDATE()) " +
                            "GROUP BY DATENAME(weekday, created_at)";

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();

                    Map<String, Integer> dayCountMap = new HashMap<>();
                    dayCountMap.put("Pzt", 0);
                    dayCountMap.put("Sal", 0);
                    dayCountMap.put("Çar", 0);
                    dayCountMap.put("Per", 0);
                    dayCountMap.put("Cum", 0);
                    dayCountMap.put("Cmt", 0);
                    dayCountMap.put("Paz", 0);

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
                            default: dayName = "Bilinmiyor"; break; // Varsayılan durum eklendi
                        }

                        dayCountMap.put(dayName, count);
                    }

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Haftalık Antrenman");

                    String[] orderedDays = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
                    for (String day : orderedDays) {
                        series.getData().add(new XYChart.Data<>(day, dayCountMap.get(day)));
                    }

                    Platform.runLater(() -> {
                        weeklyWorkoutChart.getData().clear();
                        weeklyWorkoutChart.getData().add(series);
                        weeklyWorkoutChart.setAnimated(true);
                        weeklyXAxis.setTickLabelGap(10);

                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle("-fx-bar-fill: #FF7F50;");
                                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " antrenman");
                                Tooltip.install(data.getNode(), tooltip);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Haftalık antrenman verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        // Hata durumunda örnek veriler
                        XYChart.Series<String, Number> fallbackSeries = new XYChart.Series<>();
                        fallbackSeries.setName("Haftalık Antrenman (Demo)");
                        String[] orderedDays = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
                        for (String day : orderedDays) {
                            fallbackSeries.getData().add(new XYChart.Data<>(day, (int)(Math.random() * 3))); // 0-2 arası rastgele
                        }
                        weeklyWorkoutChart.getData().clear();
                        weeklyWorkoutChart.getData().add(fallbackSeries);
                        showAlert("Uyarı", "Haftalık antrenman grafiği yüklenemedi. Demo veriler gösteriliyor.");
                    });
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
                // Veri vw_PopularExercises görünümünden gelir: kayıtlı set sayısı,
                // favorilenme ve rutinlerde kullanım. Daha önce Logs.description
                // üzerinde metin eşleştirmesi yapılıyor, veri bulunamazsa rastgele
                // sayı üretiliyordu; artık gerçek kullanım verisi gösteriliyor.
                try {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Kullanım Sayısı");

                    for (ReportingDAO.PopularExercise row : reportingDAO.findPopularExercises(5)) {
                        series.getData().add(
                                new XYChart.Data<>(row.getExerciseName(), row.getUsageCount()));
                    }

                    boolean hasData = !series.getData().isEmpty();

                    Platform.runLater(() -> {
                        if (!hasData) {
                            popularExercisesChart.getData().clear();
                            popularExercisesChart.setTitle("Henüz antrenman kaydı yok");
                            return;
                        }
                        popularExercisesChart.getData().clear();
                        popularExercisesChart.getData().add(series);
                        popularExercisesChart.setAnimated(true);
                        exerciseXAxis.setTickLabelGap(10);

                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle("-fx-bar-fill: #FF7F50;");
                                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " kullanım");
                                Tooltip.install(data.getNode(), tooltip);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Popüler egzersiz verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        // Hata durumunda uydurma veri gosterilmez; grafik bos birakilir.
                        popularExercisesChart.getData().clear();
                        popularExercisesChart.setTitle("Veri yüklenemedi");
                        showAlert("Uyarı", "Popüler egzersizler grafiği yüklenemedi: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void loadExerciseDistributionData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    String sql = "SELECT mg.name, COUNT(em.exercise_id) as exercise_count " +
                            "FROM MuscleGroups mg " +
                            "LEFT JOIN ExerciseMuscles em ON mg.id = em.muscle_id " +
                            "GROUP BY mg.name " +
                            "ORDER BY exercise_count DESC";

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();

                    List<PieChart.Data> chartData = new ArrayList<>();
                    boolean hasData = false;

                    while (rs.next()) {
                        String muscleName = rs.getString("name");
                        int exerciseCount = rs.getInt("exercise_count");

                        if (exerciseCount > 0) {
                            hasData = true;
                            chartData.add(new PieChart.Data(muscleName, exerciseCount));
                        }
                    }

                    if (!hasData) {
                        String fallbackSql = "SELECT name FROM MuscleGroups ORDER BY NEWID()";
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
                        exerciseDistributionChart.setAnimated(true);
                        exerciseDistributionChart.setLabelsVisible(true);
                        exerciseDistributionChart.setLabelLineLength(20);
                        exerciseDistributionChart.setStartAngle(90);

                        for (PieChart.Data data : chartData) {
                            data.getNode().setStyle("-fx-pie-color: " + getRandomColor() + ";");
                            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int) data.getPieValue() + " egzersiz");
                            Tooltip.install(data.getNode(), tooltip);
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Egzersiz dağılımı verisi yüklenirken hata: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        // Hata durumunda örnek veriler
                        exerciseDistributionChart.getData().clear();
                        exerciseDistributionChart.getData().add(new PieChart.Data("Göğüs (Demo)", 5));
                        exerciseDistributionChart.getData().add(new PieChart.Data("Sırt (Demo)", 3));
                        exerciseDistributionChart.getData().add(new PieChart.Data("Bacak (Demo)", 7));
                        showAlert("Uyarı", "Egzersiz dağılım grafiği yüklenemedi. Demo veriler gösteriliyor.");
                    });
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

    private void loadReports() { // Tüm genel rapor istatistiklerini ve grafikleri yükler
        System.out.println("🔄 Raporlar yükleniyor...");
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    // Gerçek kullanıcı sayısı
                    String userCountSql = "SELECT COUNT(*) as total_users FROM Users";
                    PreparedStatement userStmt = conn.prepareStatement(userCountSql);
                    ResultSet userRs = userStmt.executeQuery();
                    int totalUsers = userRs.next() ? userRs.getInt("total_users") : 0;

                    // Gerçek egzersiz sayısı
                    String exerciseCountSql = "SELECT COUNT(*) as total_exercises FROM Exercises";
                    PreparedStatement exerciseStmt = conn.prepareStatement(exerciseCountSql);
                    ResultSet exerciseRs = exerciseStmt.executeQuery();
                    int totalExercises = exerciseRs.next() ? exerciseRs.getInt("total_exercises") : 0;

                    // Son 7 günde aktif kullanıcı sayısı - Logs tablosundan
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

                    // Tüm grafik verilerini yükle
                    loadUserActivityData();
                    loadExerciseDistributionData();
                    loadWeeklyWorkoutData();
                    loadPopularExercisesData();

                } catch (Exception e) {
                    System.err.println("❌ Sistem istatistikleri yüklenirken hata: " + e.getMessage());
                    Platform.runLater(() -> {
                        totalUsersLabel.setText("0");
                        totalExercisesLabel.setText("0");
                        activeUsersLabel.setText("0");
                        showAlert("Hata", "Sistem istatistikleri yüklenemedi. Veritabanı bağlantınızı kontrol edin.");
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleRefresh() {
        loadReports(); // Tüm istatistikleri ve grafikleri yeniden yükler
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
        System.out.println("✅ SystemReportsContent: Current user set to " + (user != null ? user.getUsername() : "null"));
        // Eğer UI'da currentUser'a bağlı gösterilecek bir bilgi varsa burada güncelleyebilirsiniz.
    }

    private void showAlert(String title, String message) {
        // Platform.runLater kullanımı, UI güncellemelerinin JavaFX Application Thread'de yapılmasını sağlar.
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
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
}