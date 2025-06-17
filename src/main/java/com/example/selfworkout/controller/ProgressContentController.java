package com.example.selfworkout.controller;

import com.example.selfworkout.model.UserWorkout;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.WorkoutService;
import com.example.selfworkout.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProgressContentController extends BaseController implements Initializable {

    // FXML Elements - Zaman AralÄ±ÄŸÄ±
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button lastWeekBtn;
    @FXML private Button lastMonthBtn;
    @FXML private Button last3MonthsBtn;
    @FXML private Button updateBtn;

    // FXML Elements - Ä°statistikler
    @FXML private Label totalWorkoutsStatLabel;
    @FXML private Label totalTimeStatLabel;
    @FXML private Label avgTimeStatLabel;
    @FXML private Label completionRateStatLabel;

    // FXML Elements - Grafikler
    @FXML private LineChart<String, Number> workoutFrequencyChart;
    @FXML private CategoryAxis frequencyXAxis;
    @FXML private NumberAxis frequencyYAxis;
    @FXML private PieChart muscleGroupChart;

    // FXML Elements - Performans Analizi
    @FXML private AreaChart<String, Number> weightProgressChart;
    @FXML private CategoryAxis weightXAxis;
    @FXML private NumberAxis weightYAxis;
    @FXML private BarChart<String, Number> setsRepsChart;
    @FXML private CategoryAxis setsRepsXAxis;
    @FXML private NumberAxis setsRepsYAxis;
    @FXML private ListView<String> bestPerformancesList;

    // FXML Elements - Son Antrenmanlar
    @FXML private ComboBox<String> workoutFilterCombo;
    @FXML private TableView<UserWorkout> recentWorkoutsTable;
    @FXML private TableColumn<UserWorkout, String> dateColumn;
    @FXML private TableColumn<UserWorkout, String> exerciseColumn;
    @FXML private TableColumn<UserWorkout, Integer> setsColumn;
    @FXML private TableColumn<UserWorkout, Integer> repsColumn;
    @FXML private TableColumn<UserWorkout, Double> weightColumn;
    @FXML private TableColumn<UserWorkout, String> durationColumn;
    @FXML private TableColumn<UserWorkout, String> statusColumn;

    // FXML Elements - Hedef KarÅŸÄ±laÅŸtÄ±rmasÄ±
    @FXML private Label weeklyTargetLabel;
    @FXML private Label weeklyActualLabel;
    @FXML private ProgressBar weeklyProgressBar;
    @FXML private Label weeklyProgressLabel;
    @FXML private Label monthlyTargetLabel;
    @FXML private Label monthlyActualLabel;
    @FXML private ProgressBar monthlyProgressBar;
    @FXML private Label monthlyProgressLabel;

    // Services
    private WorkoutService workoutService;

    // Data
    private ObservableList<UserWorkout> allWorkouts;
    private ObservableList<UserWorkout> filteredWorkouts;
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        setDefaultDateRange();
        loadData();
    }

    @Override
    protected void initializeController() {
        System.out.println("ProgressContentController initialized");
    }

    private void initializeServices() {
        this.workoutService = ServiceManager.getInstance().getWorkoutService();
    }

    private void setupUI() {
        // Initialize collections
        allWorkouts = FXCollections.observableArrayList();
        filteredWorkouts = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Setup filter combo
        setupFilterCombo();
        
        // Setup charts
        setupCharts();
    }

    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getWorkoutDate();
            String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });

        // Exercise column - extract from notes or workout title
        exerciseColumn.setCellValueFactory(cellData -> {
            String notes = cellData.getValue().getNotes();
            String exercise = "Bilinmiyor";
            if (notes != null && notes.contains("Egzersiz: ")) {
                exercise = notes.substring(notes.indexOf("Egzersiz: ") + 10);
                if (exercise.contains(" | ")) {
                    exercise = exercise.substring(0, exercise.indexOf(" | "));
                }
            }
            return new javafx.beans.property.SimpleStringProperty(exercise);
        });

        // Sets column
        setsColumn.setCellValueFactory(new PropertyValueFactory<>("sets"));

        // Reps column
        repsColumn.setCellValueFactory(new PropertyValueFactory<>("reps"));

        // Weight column
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        // Duration column
        durationColumn.setCellValueFactory(cellData -> {
            int minutes = cellData.getValue().getDurationMinutes();
            String duration = minutes > 0 ? minutes + "dk" : "-";
            return new javafx.beans.property.SimpleStringProperty(duration);
        });

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String statusText = switch (status) {
                case "completed" -> "âœ… TamamlandÄ±";
                case "active" -> "ğŸ”„ Aktif";
                case "cancelled" -> "âŒ Ä°ptal";
                default -> "ğŸ“… PlanlandÄ±";
            };
            return new javafx.beans.property.SimpleStringProperty(statusText);
        });

        // Set table data
        recentWorkoutsTable.setItems(filteredWorkouts);
    }

    private void setupFilterCombo() {
        workoutFilterCombo.setItems(FXCollections.observableArrayList(
            "TÃ¼mÃ¼", "Tamamlanan", "Ä°ptal Edilen", "Aktif"
        ));
        workoutFilterCombo.setValue("TÃ¼mÃ¼");
        workoutFilterCombo.setOnAction(e -> filterWorkouts());
    }

    private void setupCharts() {
        // Workout frequency chart
        workoutFrequencyChart.setTitle("GÃ¼nlÃ¼k Antrenman SÄ±klÄ±ÄŸÄ±");
        workoutFrequencyChart.setCreateSymbols(true);
        workoutFrequencyChart.setLegendVisible(false);

        // Muscle group chart
        muscleGroupChart.setTitle("Kas Grubu DaÄŸÄ±lÄ±mÄ±");

        // Weight progress chart
        weightProgressChart.setTitle("Ortalama AÄŸÄ±rlÄ±k Ä°lerlemesi");
        weightProgressChart.setCreateSymbols(false);

        // Sets/Reps chart
        setsRepsChart.setTitle("Set ve Tekrar SayÄ±larÄ±");
    }

    private void setDefaultDateRange() {
        // Default: Last month
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusMonths(1);
        
        startDatePicker.setValue(currentStartDate);
        endDatePicker.setValue(currentEndDate);
    }

    private void loadData() {
        try {
            if (getCurrentUser() != null) {
                // Load all workouts for the user
                List<UserWorkout> userWorkouts = workoutService.getAllWorkouts().stream()
                    .filter(workout -> workout.getUserId() == getCurrentUser().getId())
                    .collect(Collectors.toList());
                
                allWorkouts.setAll(userWorkouts);
                filterWorkoutsByDateRange();
                updateStatistics();
                updateCharts();
                updateGoalProgress();
            }
        } catch (Exception e) {
            System.err.println("Error loading progress data: " + e.getMessage());
            AlertUtil.showError("Hata", "Ä°lerleme verileri yÃ¼klenirken hata oluÅŸtu: " + e.getMessage());
        }
    }

    private void filterWorkoutsByDateRange() {
        List<UserWorkout> dateFiltered = allWorkouts.stream()
            .filter(workout -> {
                LocalDate workoutDate = workout.getWorkoutDate();
                return workoutDate != null && 
                       !workoutDate.isBefore(currentStartDate) && 
                       !workoutDate.isAfter(currentEndDate);
            })
            .collect(Collectors.toList());
        
        filteredWorkouts.setAll(dateFiltered);
        filterWorkouts(); // Apply additional filters
    }

    private void filterWorkouts() {
        String filter = workoutFilterCombo.getValue();
        
        List<UserWorkout> filtered = filteredWorkouts.stream()
            .filter(workout -> {
                if ("TÃ¼mÃ¼".equals(filter)) return true;
                
                return switch (filter) {
                    case "Tamamlanan" -> "completed".equals(workout.getStatus());
                    case "Ä°ptal Edilen" -> "cancelled".equals(workout.getStatus());
                    case "Aktif" -> "active".equals(workout.getStatus());
                    default -> true;
                };
            })
            .collect(Collectors.toList());
        
        // Update table without affecting charts (charts use all filtered workouts)
        recentWorkoutsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateStatistics() {
        List<UserWorkout> completedWorkouts = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .collect(Collectors.toList());
        
        // Total workouts
        totalWorkoutsStatLabel.setText(String.valueOf(completedWorkouts.size()));
        
        // Total time
        int totalMinutes = completedWorkouts.stream()
            .mapToInt(UserWorkout::getDurationMinutes)
            .sum();
        double totalHours = totalMinutes / 60.0;
        totalTimeStatLabel.setText(String.format("%.1fh", totalHours));
        
        // Average time
        double avgMinutes = completedWorkouts.isEmpty() ? 0 : 
            completedWorkouts.stream().mapToInt(UserWorkout::getDurationMinutes).average().orElse(0);
        avgTimeStatLabel.setText(String.format("%.0fdk", avgMinutes));
        
        // Completion rate
        int totalPlanned = filteredWorkouts.size();
        double completionRate = totalPlanned > 0 ? (completedWorkouts.size() * 100.0) / totalPlanned : 0;
        completionRateStatLabel.setText(String.format("%.0f%%", completionRate));
    }

    private void updateCharts() {
        updateWorkoutFrequencyChart();
        updateMuscleGroupChart();
        updateWeightProgressChart();
        updateSetsRepsChart();
        updateBestPerformances();
    }

    private void updateWorkoutFrequencyChart() {
        workoutFrequencyChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Antrenman SayÄ±sÄ±");
        
        // Group workouts by date
        Map<LocalDate, Long> workoutsByDate = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .collect(Collectors.groupingBy(
                UserWorkout::getWorkoutDate,
                Collectors.counting()
            ));
        
        // Create data points for each day in range
        LocalDate date = currentStartDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        
        while (!date.isAfter(currentEndDate)) {
            long count = workoutsByDate.getOrDefault(date, 0L);
            series.getData().add(new XYChart.Data<>(date.format(formatter), count));
            date = date.plusDays(1);
        }
        
        workoutFrequencyChart.getData().add(series);
    }

    private void updateMuscleGroupChart() {
        muscleGroupChart.getData().clear();
        
        // Extract muscle groups from workout notes
        Map<String, Long> muscleGroupCounts = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .map(this::extractMuscleGroup)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                group -> group,
                Collectors.counting()
            ));
        
        // Create pie chart data
        for (Map.Entry<String, Long> entry : muscleGroupCounts.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            muscleGroupChart.getData().add(data);
        }
    }

    private String extractMuscleGroup(UserWorkout workout) {
        // Try to extract muscle group from notes
        // This is a simplified approach - in real app, you'd have proper exercise data
        String notes = workout.getNotes();
        if (notes == null) return "Bilinmiyor";
        
        if (notes.toLowerCase().contains("gÃ¶ÄŸÃ¼s") || notes.toLowerCase().contains("bench")) return "GÃ¶ÄŸÃ¼s";
        if (notes.toLowerCase().contains("sÄ±rt") || notes.toLowerCase().contains("pull")) return "SÄ±rt";
        if (notes.toLowerCase().contains("bacak") || notes.toLowerCase().contains("squat")) return "Bacak";
        if (notes.toLowerCase().contains("omuz") || notes.toLowerCase().contains("shoulder")) return "Omuz";
        if (notes.toLowerCase().contains("kol") || notes.toLowerCase().contains("bicep") || notes.toLowerCase().contains("tricep")) return "Kol";
        if (notes.toLowerCase().contains("karÄ±n") || notes.toLowerCase().contains("abs")) return "KarÄ±n";
        
        return "DiÄŸer";
    }

    private void updateWeightProgressChart() {
        weightProgressChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ortalama AÄŸÄ±rlÄ±k");
        
        // Group by week and calculate average weight
        Map<String, Double> weeklyAvgWeight = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()) && w.getWeight() > 0)
            .collect(Collectors.groupingBy(
                w -> getWeekLabel(w.getWorkoutDate()),
                Collectors.averagingDouble(UserWorkout::getWeight)
            ));
        
        // Sort by week and add to chart
        weeklyAvgWeight.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        weightProgressChart.getData().add(series);
    }

    private void updateSetsRepsChart() {
        setsRepsChart.getData().clear();
        
        XYChart.Series<String, Number> setsSeries = new XYChart.Series<>();
        setsSeries.setName("Ortalama Set");
        
        XYChart.Series<String, Number> repsSeries = new XYChart.Series<>();
        repsSeries.setName("Ortalama Tekrar");
        
        // Group by week and calculate averages
        Map<String, Double> weeklyAvgSets = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()) && w.getSets() > 0)
            .collect(Collectors.groupingBy(
                w -> getWeekLabel(w.getWorkoutDate()),
                Collectors.averagingInt(UserWorkout::getSets)
            ));
        
        Map<String, Double> weeklyAvgReps = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()) && w.getReps() > 0)
            .collect(Collectors.groupingBy(
                w -> getWeekLabel(w.getWorkoutDate()),
                Collectors.averagingInt(UserWorkout::getReps)
            ));
        
        // Add data to series
        weeklyAvgSets.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                setsSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        weeklyAvgReps.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                repsSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        setsRepsChart.getData().addAll(setsSeries, repsSeries);
    }

    private void updateBestPerformances() {
        List<String> performances = new ArrayList<>();
        
        List<UserWorkout> completedWorkouts = filteredWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .collect(Collectors.toList());
        
        if (!completedWorkouts.isEmpty()) {
            // Best weight
            UserWorkout maxWeight = completedWorkouts.stream()
                .max(Comparator.comparingDouble(UserWorkout::getWeight))
                .orElse(null);
            if (maxWeight != null && maxWeight.getWeight() > 0) {
                performances.add(String.format("ğŸ‹ï¸ En YÃ¼ksek AÄŸÄ±rlÄ±k: %.1f kg (%s)", 
                    maxWeight.getWeight(), 
                    maxWeight.getWorkoutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
            
            // Best sets
            UserWorkout maxSets = completedWorkouts.stream()
                .max(Comparator.comparingInt(UserWorkout::getSets))
                .orElse(null);
            if (maxSets != null && maxSets.getSets() > 0) {
                performances.add(String.format("ğŸ”¢ En Fazla Set: %d set (%s)", 
                    maxSets.getSets(), 
                    maxSets.getWorkoutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
            
            // Best reps
            UserWorkout maxReps = completedWorkouts.stream()
                .max(Comparator.comparingInt(UserWorkout::getReps))
                .orElse(null);
            if (maxReps != null && maxReps.getReps() > 0) {
                performances.add(String.format("ğŸ” En Fazla Tekrar: %d tekrar (%s)", 
                    maxReps.getReps(), 
                    maxReps.getWorkoutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
            
            // Longest workout
            UserWorkout longestWorkout = completedWorkouts.stream()
                .max(Comparator.comparingInt(UserWorkout::getDurationMinutes))
                .orElse(null);
            if (longestWorkout != null && longestWorkout.getDurationMinutes() > 0) {
                performances.add(String.format("â±ï¸ En Uzun Antrenman: %d dakika (%s)", 
                    longestWorkout.getDurationMinutes(), 
                    longestWorkout.getWorkoutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
        }
        
        bestPerformancesList.setItems(FXCollections.observableArrayList(performances));
    }

    private void updateGoalProgress() {
        // Weekly goal (default: 3 workouts per week)
        int weeklyTarget = 3;
        LocalDate weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        long weeklyActual = allWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .filter(w -> {
                LocalDate date = w.getWorkoutDate();
                return date != null && !date.isBefore(weekStart) && !date.isAfter(weekEnd);
            })
            .count();
        
        weeklyTargetLabel.setText(String.valueOf(weeklyTarget));
        weeklyActualLabel.setText(String.valueOf(weeklyActual));
        double weeklyProgress = Math.min(1.0, (double) weeklyActual / weeklyTarget);
        weeklyProgressBar.setProgress(weeklyProgress);
        weeklyProgressLabel.setText(String.format("%.0f%%", weeklyProgress * 100));
        
        // Monthly goal (default: 12 workouts per month)
        int monthlyTarget = 12;
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now();
        
        long monthlyActual = allWorkouts.stream()
            .filter(w -> "completed".equals(w.getStatus()))
            .filter(w -> {
                LocalDate date = w.getWorkoutDate();
                return date != null && !date.isBefore(monthStart) && !date.isAfter(monthEnd);
            })
            .count();
        
        monthlyTargetLabel.setText(String.valueOf(monthlyTarget));
        monthlyActualLabel.setText(String.valueOf(monthlyActual));
        double monthlyProgress = Math.min(1.0, (double) monthlyActual / monthlyTarget);
        monthlyProgressBar.setProgress(monthlyProgress);
        monthlyProgressLabel.setText(String.format("%.0f%%", monthlyProgress * 100));
    }

    private String getWeekLabel(LocalDate date) {
        // Get week number and year
        int weekOfYear = date.getDayOfYear() / 7 + 1;
        return String.format("H%d", weekOfYear);
    }

    // Event Handlers
    @FXML
    private void handleLastWeek() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusWeeks(1);
        updateDatePickers();
        handleUpdate();
    }

    @FXML
    private void handleLastMonth() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusMonths(1);
        updateDatePickers();
        handleUpdate();
    }

    @FXML
    private void handleLast3Months() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusMonths(3);
        updateDatePickers();
        handleUpdate();
    }

    @FXML
    private void handleUpdate() {
        currentStartDate = startDatePicker.getValue();
        currentEndDate = endDatePicker.getValue();
        
        if (currentStartDate == null || currentEndDate == null) {
            AlertUtil.showWarning("UyarÄ±", "LÃ¼tfen baÅŸlangÄ±Ã§ ve bitiÅŸ tarihlerini seÃ§in.");
            return;
        }
        
        if (currentStartDate.isAfter(currentEndDate)) {
            AlertUtil.showWarning("UyarÄ±", "BaÅŸlangÄ±Ã§ tarihi bitiÅŸ tarihinden sonra olamaz.");
            return;
        }
        
        filterWorkoutsByDateRange();
        updateStatistics();
        updateCharts();
        updateGoalProgress();
    }

    private void updateDatePickers() {
        startDatePicker.setValue(currentStartDate);
        endDatePicker.setValue(currentEndDate);
    }

    // Refresh method for external calls
    public void refresh() {
        loadData();
    }
} 



