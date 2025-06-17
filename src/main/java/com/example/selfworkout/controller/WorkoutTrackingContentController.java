package com.example.selfworkout.controller;

import com.example.selfworkout.controller.user.UserDashboardController;
import com.example.selfworkout.model.Workout;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.WorkoutService;
import com.example.selfworkout.service.ExerciseService;
import com.example.selfworkout.util.AlertUtil;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WorkoutTrackingContentController extends BaseController implements Initializable {

    // FXML Elements - Aktif Antrenman Paneli
    @FXML private VBox activeWorkoutPanel;
    @FXML private Label exerciseNameLabel;
    @FXML private Label exerciseDetailsLabel;
    @FXML private Label durationLabel;
    @FXML private Label plannedSetsLabel;
    @FXML private Label plannedRepsLabel;
    @FXML private Label plannedWeightLabel;
    @FXML private Label completedSetsLabel;
    @FXML private Label completedRepsLabel;
    @FXML private Label actualWeightLabel;
    @FXML private Label progressPercentageLabel;
    @FXML private ProgressBar workoutProgressBar;

    // FXML Elements - Set GiriÅŸi
    @FXML private Spinner<Integer> repsSpinner;
    @FXML private Spinner<Double> weightSpinner;
    @FXML private Spinner<Integer> restSpinner;
    @FXML private Button addSetBtn;

    // FXML Elements - Set GeÃ§miÅŸi
    @FXML private ListView<WorkoutSet> setsListView;

    // FXML Elements - Notlar ve Butonlar
    @FXML private TextArea notesTextArea;
    @FXML private Button pauseWorkoutBtn;
    @FXML private Button completeWorkoutBtn;
    @FXML private Button cancelWorkoutBtn;

    // FXML Elements - Aktif Antrenman Yok Paneli
    @FXML private VBox noActiveWorkoutPanel;
    @FXML private Button startNewWorkoutBtn;

    // Services
    private WorkoutService workoutService;
    private ExerciseService exerciseService;

    // Data
    private Workout activeWorkout;
    private Exercise currentExercise;
    private ObservableList<WorkoutSet> completedSets;
    private Timeline durationTimer;
    private LocalDateTime workoutStartTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        loadActiveWorkout();
        setupTimer();
    }

    @Override
    protected void initializeController() {
        System.out.println("WorkoutTrackingContentController initialized");
    }

    private void initializeServices() {
        this.workoutService = ServiceManager.getInstance().getWorkoutService();
        this.exerciseService = ServiceManager.getInstance().getExerciseService();
    }

    private void setupUI() {
        // Initialize collections
        completedSets = FXCollections.observableArrayList();
        
        // Setup Spinners
        setupSpinners();
        
        // Setup ListView
        setupSetsListView();
        
        // Initially hide active workout panel
        showNoActiveWorkout();
    }

    private void setupSpinners() {
        // Reps spinner (1-50)
        repsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 12));
        repsSpinner.setEditable(true);
        
        // Weight spinner (0-500 kg, 0.5 step)
        weightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 500.0, 20.0, 0.5));
        weightSpinner.setEditable(true);
        
        // Rest spinner (30-600 seconds, 15 step)
        restSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 600, 90, 15));
        restSpinner.setEditable(true);
    }

    private void setupSetsListView() {
        setsListView.setItems(completedSets);
        
        // Custom cell factory for better display
        setsListView.setCellFactory(listView -> new ListCell<WorkoutSet>() {
            @Override
            protected void updateItem(WorkoutSet set, boolean empty) {
                super.updateItem(set, empty);
                
                if (empty || set == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String displayText = String.format("Set %d: %d tekrar Ã— %.1f kg (%s)", 
                        set.getSetNumber(),
                        set.getReps(),
                        set.getWeight(),
                        set.getCompletedTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    );
                    setText(displayText);
                    
                    // Style based on performance
                    if (activeWorkout != null) {
                        boolean isGoodPerformance = set.getReps() >= activeWorkout.getPlannedReps() && 
                                                  set.getWeight() >= activeWorkout.getWeight();
                        if (isGoodPerformance) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                        }
                    }
                }
            }
        });
    }

    private void setupTimer() {
        durationTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDuration()));
        durationTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateDuration() {
        if (workoutStartTime != null) {
            long seconds = java.time.Duration.between(workoutStartTime, LocalDateTime.now()).getSeconds();
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            
            Platform.runLater(() -> {
                durationLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
            });
        }
    }

    private void loadActiveWorkout() {
        try {
            if (getCurrentUser() != null) {
                activeWorkout = workoutService.getActiveWorkout(getCurrentUser().getId());
                
                if (activeWorkout != null) {
                    loadExerciseDetails();
                    showActiveWorkout();
                    startTimer();
                } else {
                    showNoActiveWorkout();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading active workout: " + e.getMessage());
            showNoActiveWorkout();
        }
    }

    private void loadExerciseDetails() {
        try {
            currentExercise = exerciseService.getExerciseById(activeWorkout.getExerciseId());
            
            if (currentExercise != null) {
                // Update UI with exercise details
                exerciseNameLabel.setText(currentExercise.getName());
                exerciseDetailsLabel.setText(currentExercise.getMuscleGroup() + " â€¢ " + currentExercise.getDifficulty());
                
                // Update planned values
                plannedSetsLabel.setText(String.valueOf(activeWorkout.getPlannedSets()));
                plannedRepsLabel.setText(String.valueOf(activeWorkout.getPlannedReps()));
                plannedWeightLabel.setText(String.format("%.1f", activeWorkout.getWeight()));
                
                // Set spinner defaults to planned values
                repsSpinner.getValueFactory().setValue(activeWorkout.getPlannedReps());
                weightSpinner.getValueFactory().setValue(activeWorkout.getWeight());
                
                // Load existing notes
                if (activeWorkout.getNotes() != null) {
                    notesTextArea.setText(activeWorkout.getNotes());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading exercise details: " + e.getMessage());
        }
    }

    private void showActiveWorkout() {
        activeWorkoutPanel.setVisible(true);
        activeWorkoutPanel.setManaged(true);
        noActiveWorkoutPanel.setVisible(false);
        noActiveWorkoutPanel.setManaged(false);
        
        updateProgress();
    }

    private void showNoActiveWorkout() {
        activeWorkoutPanel.setVisible(false);
        activeWorkoutPanel.setManaged(false);
        noActiveWorkoutPanel.setVisible(true);
        noActiveWorkoutPanel.setManaged(true);
        
        stopTimer();
    }

    private void startTimer() {
        if (activeWorkout != null && activeWorkout.getStartTime() != null) {
            workoutStartTime = activeWorkout.getStartTime();
            durationTimer.play();
        }
    }

    private void stopTimer() {
        if (durationTimer != null) {
            durationTimer.stop();
        }
    }

    private void updateProgress() {
        if (activeWorkout == null) return;
        
        int completedSetsCount = completedSets.size();
        int plannedSetsCount = activeWorkout.getPlannedSets();
        
        // Update completed values
        completedSetsLabel.setText(String.valueOf(completedSetsCount));
        
        // Calculate average reps and weight
        if (completedSetsCount > 0) {
            double avgReps = completedSets.stream().mapToInt(WorkoutSet::getReps).average().orElse(0);
            double avgWeight = completedSets.stream().mapToDouble(WorkoutSet::getWeight).average().orElse(0);
            
            completedRepsLabel.setText(String.format("%.0f", avgReps));
            actualWeightLabel.setText(String.format("%.1f", avgWeight));
        } else {
            completedRepsLabel.setText("0");
            actualWeightLabel.setText("0.0");
        }
        
        // Update progress bar and percentage
        double progress = plannedSetsCount > 0 ? (double) completedSetsCount / plannedSetsCount : 0;
        workoutProgressBar.setProgress(progress);
        progressPercentageLabel.setText(String.format("%.0f%%", progress * 100));
    }

    @FXML
    private void handleAddSet() {
        if (activeWorkout == null) {
            AlertUtil.showWarning("UyarÄ±", "Aktif antrenman bulunamadÄ±.");
            return;
        }
        
        try {
            int reps = repsSpinner.getValue();
            double weight = weightSpinner.getValue();
            int restTime = restSpinner.getValue();
            
            // Create new set
            WorkoutSet newSet = new WorkoutSet();
            newSet.setSetNumber(completedSets.size() + 1);
            newSet.setReps(reps);
            newSet.setWeight(weight);
            newSet.setRestTime(restTime);
            newSet.setCompletedTime(LocalDateTime.now());
            
            // Add to list
            completedSets.add(newSet);
            
            // Update progress
            updateProgress();
            
            // Show success message
            AlertUtil.showSuccess("BaÅŸarÄ±lÄ±", String.format("Set %d eklendi: %d tekrar Ã— %.1f kg", 
                newSet.getSetNumber(), reps, weight));
            
            // Check if workout is complete
            if (completedSets.size() >= activeWorkout.getPlannedSets()) {
                boolean autoComplete = AlertUtil.showConfirmation("Antrenman TamamlandÄ±", 
                    "Planlanan set sayÄ±sÄ±na ulaÅŸtÄ±nÄ±z. AntrenmanÄ± tamamlamak ister misiniz?");
                
                if (autoComplete) {
                    handleCompleteWorkout();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error adding set: " + e.getMessage());
            AlertUtil.showError("Hata", "Set eklenirken hata oluÅŸtu: " + e.getMessage());
        }
    }

    @FXML
    private void handlePauseWorkout() {
        AlertUtil.showInfo("Bilgi", "Antrenman duraklatma Ã¶zelliÄŸi yakÄ±nda eklenecek!");
    }

    @FXML
    private void handleCompleteWorkout() {
        if (activeWorkout == null) {
            AlertUtil.showWarning("UyarÄ±", "Aktif antrenman bulunamadÄ±.");
            return;
        }
        
        if (completedSets.isEmpty()) {
            boolean confirm = AlertUtil.showConfirmation("UyarÄ±", 
                "HiÃ§ set eklemediniz. Yine de antrenmanÄ± tamamlamak ister misiniz?");
            if (!confirm) return;
        }
        
        try {
            // Calculate totals
            int totalSets = completedSets.size();
            int totalReps = completedSets.stream().mapToInt(WorkoutSet::getReps).sum();
            double avgWeight = completedSets.isEmpty() ? 0 : 
                completedSets.stream().mapToDouble(WorkoutSet::getWeight).average().orElse(0);
            
            String notes = notesTextArea.getText().trim();
            
            // Complete workout
            boolean success = workoutService.completeWorkout(
                activeWorkout.getId(), 
                totalSets, 
                totalReps, 
                avgWeight, 
                notes
            );
            
            if (success) {
                AlertUtil.showSuccess("Tebrikler!", 
                    String.format("Antrenman baÅŸarÄ±yla tamamlandÄ±!\n\nÃ–zet:\n- %d set\n- %d toplam tekrar\n- %.1f kg ortalama aÄŸÄ±rlÄ±k", 
                        totalSets, totalReps, avgWeight));
                
                // Reset UI
                activeWorkout = null;
                completedSets.clear();
                showNoActiveWorkout();
                
            } else {
                AlertUtil.showError("Hata", "Antrenman tamamlanÄ±rken hata oluÅŸtu.");
            }
            
        } catch (Exception e) {
            System.err.println("Error completing workout: " + e.getMessage());
            AlertUtil.showError("Hata", "Antrenman tamamlanÄ±rken hata oluÅŸtu: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelWorkout() {
        if (activeWorkout == null) {
            AlertUtil.showWarning("UyarÄ±", "Aktif antrenman bulunamadÄ±.");
            return;
        }
        
        boolean confirm = AlertUtil.showConfirmation("Antrenman Ä°ptali", 
            "AntrenmanÄ± iptal etmek istediÄŸinizden emin misiniz?\nTÃ¼m verileriniz kaybolacaktÄ±r.");
        
        if (confirm) {
            try {
                workoutService.cancelWorkout(activeWorkout.getId());
                
                AlertUtil.showInfo("Ä°ptal Edildi", "Antrenman iptal edildi.");
                
                // Reset UI
                activeWorkout = null;
                completedSets.clear();
                showNoActiveWorkout();
                
            } catch (Exception e) {
                System.err.println("Error canceling workout: " + e.getMessage());
                AlertUtil.showError("Hata", "Antrenman iptal edilirken hata oluÅŸtu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleStartNewWorkout() {
        // Navigate to workout start page
        try {
            if (getParent() instanceof UserDashboardController) {
                UserDashboardController parentController = (UserDashboardController) getParent();
                parentController.loadWorkoutStartContent();
            }
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
            AlertUtil.showError("Hata", "Antrenman baÅŸlatma sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
        }
    }

    // Refresh method for external calls
    public void refresh() {
        loadActiveWorkout();
    }

    // Inner class for workout sets
    public static class WorkoutSet {
        private int setNumber;
        private int reps;
        private double weight;
        private int restTime;
        private LocalDateTime completedTime;

        // Getters and Setters
        public int getSetNumber() { return setNumber; }
        public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

        public int getReps() { return reps; }
        public void setReps(int reps) { this.reps = reps; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }

        public int getRestTime() { return restTime; }
        public void setRestTime(int restTime) { this.restTime = restTime; }

        public LocalDateTime getCompletedTime() { return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    }
} 



