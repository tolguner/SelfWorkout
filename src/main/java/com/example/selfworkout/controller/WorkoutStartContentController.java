package com.example.selfworkout.controller;

import com.example.selfworkout.controller.user.UserDashboardController;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Workout;
import com.example.selfworkout.service.ExerciseService;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.WorkoutService;
import com.example.selfworkout.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WorkoutStartContentController extends BaseController implements Initializable {

    // FXML Elements
    @FXML private VBox activeWorkoutWarning;
    @FXML private Label activeWorkoutLabel;
    @FXML private Button continueWorkoutBtn;
    @FXML private Button cancelActiveWorkoutBtn;
    
    @FXML private TextField exerciseSearchField;
    @FXML private ComboBox<String> muscleGroupCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private Button searchBtn;
    @FXML private ListView<Exercise> exerciseListView;
    
    @FXML private VBox selectedExercisePanel;
    @FXML private Label selectedExerciseName;
    @FXML private Label selectedExerciseDescription;
    @FXML private Label selectedExerciseDifficulty;
    @FXML private Label selectedExerciseMuscles;
    
    @FXML private Spinner<Integer> setsSpinner;
    @FXML private Spinner<Integer> repsSpinner;
    @FXML private Spinner<Double> weightSpinner;
    @FXML private TextArea notesTextArea;
    
    @FXML private Button startWorkoutBtn;
    @FXML private Button clearSelectionBtn;

    // Services
    private ExerciseService exerciseService;
    private WorkoutService workoutService;
    
    // Data
    private ObservableList<Exercise> allExercises;
    private ObservableList<Exercise> filteredExercises;
    private Exercise selectedExercise;
    private Workout activeWorkout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        loadExercises();
        checkActiveWorkout();
        setupEventHandlers();
    }

    @Override
    protected void initializeController() {
        // BaseController'dan gelen abstract metod - burada Ã¶zel initialization yapÄ±labilir
        System.out.println("WorkoutStartContentController initialized");
    }

    private void initializeServices() {
        this.exerciseService = ServiceManager.getInstance().getExerciseService();
        this.workoutService = ServiceManager.getInstance().getWorkoutService();
    }

    private void setupUI() {
        // Initialize collections
        allExercises = FXCollections.observableArrayList();
        filteredExercises = FXCollections.observableArrayList();
        
        // Setup ComboBoxes
        setupComboBoxes();
        
        // Setup Spinners
        setupSpinners();
        
        // Setup ListView
        setupExerciseListView();
        
        // Initially hide selected exercise panel
        selectedExercisePanel.setVisible(false);
        selectedExercisePanel.setManaged(false);
    }

    private void setupComboBoxes() {
        // Muscle groups
        muscleGroupCombo.getItems().addAll(
            "TÃ¼mÃ¼", "GÃ¶ÄŸÃ¼s", "SÄ±rt", "Omuz", "Biceps", "Triceps", 
            "Bacak", "KarÄ±n", "KalÃ§a", "BaldÄ±r"
        );
        muscleGroupCombo.setValue("TÃ¼mÃ¼");
        
        // Difficulty levels
        difficultyCombo.getItems().addAll("TÃ¼mÃ¼", "Kolay", "Orta", "Zor");
        difficultyCombo.setValue("TÃ¼mÃ¼");
    }

    private void setupSpinners() {
        // Sets spinner (1-10)
        setsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        setsSpinner.setEditable(true);
        
        // Reps spinner (1-50)
        repsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 12));
        repsSpinner.setEditable(true);
        
        // Weight spinner (0-500 kg, 0.5 step)
        weightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 500.0, 20.0, 0.5));
        weightSpinner.setEditable(true);
    }

    private void setupExerciseListView() {
        exerciseListView.setItems(filteredExercises);
        
        // Custom cell factory for better display
        exerciseListView.setCellFactory(new Callback<ListView<Exercise>, ListCell<Exercise>>() {
            @Override
            public ListCell<Exercise> call(ListView<Exercise> param) {
                return new ListCell<Exercise>() {
                    @Override
                    protected void updateItem(Exercise exercise, boolean empty) {
                        super.updateItem(exercise, empty);
                        
                        if (empty || exercise == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            // Create display text
                            String displayText = String.format("%s\n%s â€¢ %s", 
                                exercise.getName(),
                                exercise.getMuscleGroup(),
                                exercise.getDifficulty()
                            );
                            setText(displayText);
                            
                            // Style based on difficulty
                            String difficultyColor = getDifficultyColor(exercise.getDifficulty());
                            setStyle(String.format(
                                "-fx-padding: 12px; -fx-border-color: %s; -fx-border-width: 0 0 0 4px; -fx-background-color: white;",
                                difficultyColor
                            ));
                        }
                    }
                };
            }
        });
        
        // Selection handler
        exerciseListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectExercise(newValue);
                }
            }
        );
    }

    private String getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "kolay": return "#27ae60";
            case "orta": return "#f39c12";
            case "zor": return "#e74c3c";
            default: return "#95a5a6";
        }
    }

    private void setupEventHandlers() {
        // Real-time search
        exerciseSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::filterExercises);
        });
        
        // ComboBox change handlers
        muscleGroupCombo.setOnAction(e -> filterExercises());
        difficultyCombo.setOnAction(e -> filterExercises());
    }

    private void loadExercises() {
        try {
            List<Exercise> exercises = exerciseService.getAllExercises();
            allExercises.setAll(exercises);
            filteredExercises.setAll(exercises);
            
            System.out.println("Loaded " + exercises.size() + " exercises");
        } catch (Exception e) {
            System.err.println("Error loading exercises: " + e.getMessage());
            AlertUtil.showError("Hata", "Egzersizler yÃ¼klenirken hata oluÅŸtu: " + e.getMessage());
        }
    }

    private void checkActiveWorkout() {
        try {
            if (getCurrentUser() != null) {
                activeWorkout = workoutService.getActiveWorkout(getCurrentUser().getId());
                
                if (activeWorkout != null) {
                    showActiveWorkoutWarning();
                } else {
                    hideActiveWorkoutWarning();
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking active workout: " + e.getMessage());
        }
    }

    private void showActiveWorkoutWarning() {
        if (activeWorkout != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            String startTime = activeWorkout.getStartTime().format(formatter);
            
            activeWorkoutLabel.setText(String.format(
                "BaÅŸlangÄ±Ã§: %s â€¢ Egzersiz: %s", 
                startTime, 
                activeWorkout.getExerciseName()
            ));
            
            activeWorkoutWarning.setVisible(true);
            activeWorkoutWarning.setManaged(true);
        }
    }

    private void hideActiveWorkoutWarning() {
        activeWorkoutWarning.setVisible(false);
        activeWorkoutWarning.setManaged(false);
    }

    @FXML
    private void handleSearch() {
        filterExercises();
    }

    private void filterExercises() {
        String searchText = exerciseSearchField.getText().toLowerCase().trim();
        String selectedMuscleGroup = muscleGroupCombo.getValue();
        String selectedDifficulty = difficultyCombo.getValue();
        
        List<Exercise> filtered = allExercises.stream()
            .filter(exercise -> {
                // Text search
                boolean matchesText = searchText.isEmpty() || 
                    exercise.getName().toLowerCase().contains(searchText) ||
                    exercise.getDescription().toLowerCase().contains(searchText);
                
                // Muscle group filter
                boolean matchesMuscleGroup = "TÃ¼mÃ¼".equals(selectedMuscleGroup) ||
                    exercise.getMuscleGroup().equals(selectedMuscleGroup);
                
                // Difficulty filter
                boolean matchesDifficulty = "TÃ¼mÃ¼".equals(selectedDifficulty) ||
                    exercise.getDifficulty().equals(selectedDifficulty);
                
                return matchesText && matchesMuscleGroup && matchesDifficulty;
            })
            .collect(Collectors.toList());
        
        filteredExercises.setAll(filtered);
    }

    private void selectExercise(Exercise exercise) {
        this.selectedExercise = exercise;
        
        // Update UI
        selectedExerciseName.setText(exercise.getName());
        selectedExerciseDescription.setText(exercise.getDescription());
        selectedExerciseDifficulty.setText(exercise.getDifficulty());
        selectedExerciseMuscles.setText(exercise.getMuscleGroup());
        
        // Set difficulty color
        String difficultyColor = getDifficultyColor(exercise.getDifficulty());
        selectedExerciseDifficulty.setStyle("-fx-text-fill: " + difficultyColor + "; -fx-font-weight: bold;");
        
        // Show selected exercise panel
        selectedExercisePanel.setVisible(true);
        selectedExercisePanel.setManaged(true);
        
        // Clear previous notes
        notesTextArea.clear();
    }

    @FXML
    private void handleStartWorkout() {
        if (selectedExercise == null) {
            AlertUtil.showWarning("UyarÄ±", "LÃ¼tfen bir egzersiz seÃ§in.");
            return;
        }
        
        if (getCurrentUser() == null) {
            AlertUtil.showError("Hata", "KullanÄ±cÄ± bilgisi bulunamadÄ±.");
            return;
        }
        
        try {
            // Get parameters
            int sets = setsSpinner.getValue();
            int reps = repsSpinner.getValue();
            double weight = weightSpinner.getValue();
            String notes = notesTextArea.getText().trim();
            
            // Create workout
            Workout workout = new Workout();
            workout.setUserId(getCurrentUser().getId());
            workout.setExerciseId(selectedExercise.getId());
            workout.setExerciseName(selectedExercise.getName());
            workout.setPlannedSets(sets);
            workout.setPlannedReps(reps);
            workout.setWeight(weight);
            workout.setNotes(notes);
            workout.setStartTime(LocalDateTime.now());
            workout.setStatus("ACTIVE");
            
            // Save workout
            workoutService.createWorkout(workout);
            
            // Show success message
            AlertUtil.showSuccess("BaÅŸarÄ±lÄ±", 
                String.format("Antrenman baÅŸlatÄ±ldÄ±!\n\nEgzersiz: %s\nSet: %d x Tekrar: %d\nAÄŸÄ±rlÄ±k: %.1f kg", 
                    selectedExercise.getName(), sets, reps, weight));
            
            // Refresh active workout check
            checkActiveWorkout();
            
            // Clear selection
            handleClearSelection();
            
        } catch (Exception e) {
            System.err.println("Error starting workout: " + e.getMessage());
            AlertUtil.showError("Hata", "Antrenman baÅŸlatÄ±lÄ±rken hata oluÅŸtu: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSelection() {
        selectedExercise = null;
        exerciseListView.getSelectionModel().clearSelection();
        
        selectedExercisePanel.setVisible(false);
        selectedExercisePanel.setManaged(false);
        
        // Reset spinners to default values
        setsSpinner.getValueFactory().setValue(3);
        repsSpinner.getValueFactory().setValue(12);
        weightSpinner.getValueFactory().setValue(20.0);
        
        // Clear notes
        notesTextArea.clear();
    }

    @FXML
    private void handleContinueWorkout() {
        if (activeWorkout != null) {
            // Navigate to workout tracking page
            try {
                if (getParent() instanceof UserDashboardController) {
                    UserDashboardController parentController = (UserDashboardController) getParent();
                    parentController.loadWorkoutTrackingContent();
                }
            } catch (Exception e) {
                System.err.println("Navigation error: " + e.getMessage());
                AlertUtil.showError("Hata", "Antrenman takip sayfasÄ±na geÃ§iÅŸ yapÄ±lamadÄ±.");
            }
        }
    }

    @FXML
    private void handleCancelActiveWorkout() {
        if (activeWorkout == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Antrenman Ä°ptali");
        confirmAlert.setHeaderText("Aktif antrenmanÄ± iptal etmek istediÄŸinizden emin misiniz?");
        confirmAlert.setContentText("Bu iÅŸlem geri alÄ±namaz ve antrenman verileriniz kaybedilecektir.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    workoutService.cancelWorkout(activeWorkout.getId());
                    AlertUtil.showSuccess("BaÅŸarÄ±lÄ±", "Antrenman iptal edildi.");
                    
                    // Refresh active workout check
                    checkActiveWorkout();
                    
                } catch (Exception e) {
                    System.err.println("Error canceling workout: " + e.getMessage());
                    AlertUtil.showError("Hata", "Antrenman iptal edilirken hata oluÅŸtu: " + e.getMessage());
                }
            }
        });
    }

    // Refresh method for external calls
    public void refresh() {
        loadExercises();
        checkActiveWorkout();
        handleClearSelection();
    }
} 



