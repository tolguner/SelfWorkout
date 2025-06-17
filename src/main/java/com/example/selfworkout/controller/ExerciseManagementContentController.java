package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.service.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;

public class ExerciseManagementContentController implements Initializable {

    // Stats Labels
    @FXML private Label totalExercisesLabel;
    @FXML private Label totalEquipmentLabel;
    @FXML private Label totalMuscleGroupsLabel;

    // Tab Navigation Buttons
    @FXML private Button exercisesTabBtn;
    @FXML private Button equipmentTabBtn;
    @FXML private Button muscleGroupsTabBtn;
    
    // Tab Content Areas
    @FXML private StackPane tabContentArea;
    @FXML private VBox exercisesTabContent;
    @FXML private VBox equipmentTabContent;
    @FXML private VBox muscleGroupsTabContent;

    // Exercises Tab Elements
    @FXML private ComboBox<String> muscleGroupFilter;
    @FXML private ComboBox<String> equipmentFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private TextField exerciseSearchField;
    @FXML private Button addExerciseBtn;
    @FXML private Button refreshExercisesBtn;
    @FXML private Button clearFiltersBtn;
    @FXML private TableView<Exercise> exercisesTable;
    @FXML private TableColumn<Exercise, Integer> exerciseIdColumn;
    @FXML private TableColumn<Exercise, String> exerciseNameColumn;
    @FXML private TableColumn<Exercise, String> exerciseDescriptionColumn;
    @FXML private TableColumn<Exercise, String> exerciseMuscleGroupColumn;
    @FXML private TableColumn<Exercise, String> exerciseEquipmentColumn;
    @FXML private TableColumn<Exercise, String> exerciseDifficultyColumn;
    @FXML private TableColumn<Exercise, Void> exerciseActionsColumn;

    // Equipment Tab Elements
    @FXML private TextField equipmentSearchField;
    @FXML private Button addEquipmentBtn;
    @FXML private Button refreshEquipmentBtn;
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Integer> equipmentIdColumn;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, String> equipmentDescriptionColumn;
    @FXML private TableColumn<Equipment, Integer> equipmentUsageCountColumn;
    @FXML private TableColumn<Equipment, String> equipmentStatusColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionsColumn;
    
    // Muscle Groups Tab Elements
    @FXML private TextField muscleGroupSearchField;
    @FXML private Button addMuscleGroupBtn;
    @FXML private Button refreshMuscleGroupsBtn;
    @FXML private TableView<MuscleGroup> muscleGroupsTable;
    @FXML private TableColumn<MuscleGroup, Integer> muscleGroupIdColumn;
    @FXML private TableColumn<MuscleGroup, String> muscleGroupNameColumn;
    @FXML private TableColumn<MuscleGroup, String> muscleGroupDescriptionColumn;
    @FXML private TableColumn<MuscleGroup, Integer> muscleGroupExerciseCountColumn;
    @FXML private TableColumn<MuscleGroup, Void> muscleGroupActionsColumn;

    // Loading Indicator
    @FXML private ProgressIndicator loadingIndicator;
    
    // Services
    private ServiceManager serviceManager;
    private ExerciseService exerciseService;
    private MuscleGroupService muscleGroupService;
    private EquipmentService equipmentService;
    private User currentUser;

    // Data Lists
    private ObservableList<Exercise> exerciseList = FXCollections.observableArrayList();
    private ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
    private ObservableList<MuscleGroup> muscleGroupList = FXCollections.observableArrayList();

    // Master lists for filtering
    private List<Exercise> allExercises;
    private List<Equipment> allEquipment;
    private List<MuscleGroup> allMuscleGroups;

    // Current Active Tab
    private String currentTab = "exercises";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupComponents();
        setupTables();
        loadData();
        showExercisesTab(); // Show exercises tab by default
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            exerciseService = serviceManager.getExerciseService();
            muscleGroupService = serviceManager.getMuscleGroupService();
            equipmentService = serviceManager.getEquipmentService();
            currentUser = serviceManager.getAuthenticationService().getCurrentUser();
        } catch (Exception e) {
            System.err.println("âŒ ExerciseManagementContent Service initialization hatasÄ±: " + e.getMessage());
        }
    }
    
    private void setupComponents() {
        // Setup filters
        setupFilters();
        
        // Setup search listeners
        setupSearchListeners();
    }

    private void setupFilters() {
        // Difficulty filter
        if (difficultyFilter != null) {
            difficultyFilter.setItems(FXCollections.observableArrayList(
                "TÃ¼mÃ¼", "BaÅŸlangÄ±Ã§", "Orta", "Ä°leri", "Uzman"
            ));
            difficultyFilter.setValue("TÃ¼mÃ¼");
        }

        // Muscle group filter - will be populated from database
        if (muscleGroupFilter != null) {
            muscleGroupFilter.setValue("TÃ¼mÃ¼");
        }

        // Equipment filter - will be populated from database
        if (equipmentFilter != null) {
            equipmentFilter.setValue("TÃ¼mÃ¼");
        }
    }

    private void setupSearchListeners() {
        // Exercise search
        if (exerciseSearchField != null) {
            exerciseSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterExercises();
            });
        }

        // Equipment search
        if (equipmentSearchField != null) {
            equipmentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterEquipment();
            });
        }

        // Muscle group search
        if (muscleGroupSearchField != null) {
            muscleGroupSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterMuscleGroups();
            });
        }

        // Filter listeners
        if (muscleGroupFilter != null) {
            muscleGroupFilter.setOnAction(e -> filterExercises());
        }
        if (equipmentFilter != null) {
            equipmentFilter.setOnAction(e -> filterExercises());
        }
        if (difficultyFilter != null) {
            difficultyFilter.setOnAction(e -> filterExercises());
        }
    }

    private void setupTables() {
        setupExercisesTable();
        setupEquipmentTable();
        setupMuscleGroupsTable();
    }

    private void setupExercisesTable() {
        if (exercisesTable == null) return;

        // Configure table style
        exercisesTable.setStyle("-fx-background-color: transparent;");
        
        // Setup columns
        if (exerciseIdColumn != null) {
            exerciseIdColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        }
        
        if (exerciseNameColumn != null) {
            exerciseNameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        if (exerciseDescriptionColumn != null) {
            exerciseDescriptionColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        }
        
        if (exerciseMuscleGroupColumn != null) {
            exerciseMuscleGroupColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMuscleGroupsAsString()));
        }
        
        if (exerciseEquipmentColumn != null) {
            exerciseEquipmentColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEquipmentsAsString()));
        }
        
        if (exerciseDifficultyColumn != null) {
            exerciseDifficultyColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDifficultyLevel() != null ? cellData.getValue().getDifficultyLevel() : "-"));
        }

        // Setup action buttons
        if (exerciseActionsColumn != null) {
            exerciseActionsColumn.setCellFactory(param -> new TableCell<Exercise, Void>() {
                private final Button editBtn = new Button("âœï¸");
                private final Button deleteBtn = new Button("ğŸ—‘ï¸");

                {
                    editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    
                    editBtn.setOnAction(e -> {
                        Exercise exercise = getTableView().getItems().get(getIndex());
                        handleEditExercise(exercise);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        Exercise exercise = getTableView().getItems().get(getIndex());
                        handleDeleteExercise(exercise);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                        buttons.setAlignment(Pos.CENTER);
                        buttons.getChildren().addAll(editBtn, deleteBtn);
                        setGraphic(buttons);
                    }
                }
            });
        }

        exercisesTable.setItems(exerciseList);
    }

    private void setupEquipmentTable() {
        if (equipmentTable == null) return;

        equipmentTable.setStyle("-fx-background-color: transparent;");
        
        // Setup columns
        if (equipmentIdColumn != null) {
            equipmentIdColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        }
        
        if (equipmentNameColumn != null) {
            equipmentNameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        if (equipmentDescriptionColumn != null) {
            equipmentDescriptionColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "-"));
        }
        
        if (equipmentUsageCountColumn != null) {
            equipmentUsageCountColumn.setCellValueFactory(cellData -> {
                try {
                    int count = equipmentService.getExerciseCountForEquipment(cellData.getValue().getId());
                    return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
                }
            });
        }
        
        if (equipmentStatusColumn != null) {
            equipmentStatusColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty("Aktif")); // Default status
        }

        // Setup action buttons for equipment
        if (equipmentActionsColumn != null) {
            equipmentActionsColumn.setCellFactory(param -> new TableCell<Equipment, Void>() {
                private final Button editBtn = new Button("âœï¸");
                private final Button deleteBtn = new Button("ğŸ—‘ï¸");

                {
                    editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    
                    editBtn.setOnAction(e -> {
                        Equipment equipment = getTableView().getItems().get(getIndex());
                        handleEditEquipment(equipment);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        Equipment equipment = getTableView().getItems().get(getIndex());
                        handleDeleteEquipment(equipment);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                        buttons.setAlignment(Pos.CENTER);
                        buttons.getChildren().addAll(editBtn, deleteBtn);
                        setGraphic(buttons);
                    }
                }
            });
        }
        
        equipmentTable.setItems(equipmentList);
    }
    
    private void setupMuscleGroupsTable() {
        if (muscleGroupsTable == null) return;

        muscleGroupsTable.setStyle("-fx-background-color: transparent;");
        
        // Setup columns
        if (muscleGroupIdColumn != null) {
            muscleGroupIdColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        }
        
        if (muscleGroupNameColumn != null) {
            muscleGroupNameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        if (muscleGroupDescriptionColumn != null) {
            muscleGroupDescriptionColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "-"));
        }
        
        if (muscleGroupExerciseCountColumn != null) {
            muscleGroupExerciseCountColumn.setCellValueFactory(cellData -> {
                try {
                    int count = muscleGroupService.getExerciseCountForMuscleGroup(cellData.getValue().getId());
                    return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
                }
            });
        }

        // Setup action buttons for muscle groups
        if (muscleGroupActionsColumn != null) {
            muscleGroupActionsColumn.setCellFactory(param -> new TableCell<MuscleGroup, Void>() {
                private final Button editBtn = new Button("âœï¸");
                private final Button deleteBtn = new Button("ğŸ—‘ï¸");

                {
                    editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                    
                    editBtn.setOnAction(e -> {
                        MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                        handleEditMuscleGroup(muscleGroup);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                        handleDeleteMuscleGroup(muscleGroup);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                        buttons.setAlignment(Pos.CENTER);
                        buttons.getChildren().addAll(editBtn, deleteBtn);
                        setGraphic(buttons);
                    }
                }
            });
        }
        
        muscleGroupsTable.setItems(muscleGroupList);
    }
    
    private void loadData() {
        setLoading(true);
        
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Load all data from services
                    allExercises = exerciseService.getAllExercises();
                    allEquipment = equipmentService.getAllEquipment();
                    allMuscleGroups = muscleGroupService.getAllMuscleGroups();
                    
                    Platform.runLater(() -> {
                        // Update lists
                        exerciseList.clear();
                        exerciseList.addAll(allExercises);
                        
                        equipmentList.clear();
                        equipmentList.addAll(allEquipment);
                        
                        muscleGroupList.clear();
                        muscleGroupList.addAll(allMuscleGroups);
                        
                        // Update stats
                        updateStats();
                        
                        // Populate filters
                        populateFilters();
                        
                        System.out.println("âœ… Exercise management data loaded successfully");
                    });
                    
                } catch (Exception e) {
                    System.err.println("âŒ Data loading error: " + e.getMessage());
                    Platform.runLater(() -> {
                        showAlert("Hata", "Veri yÃ¼klenirken hata oluÅŸtu: " + e.getMessage());
                    });
                }
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> setLoading(false));
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> setLoading(false));
            }
        };
        
        new Thread(loadTask).start();
    }

    private void updateStats() {
        try {
            if (totalExercisesLabel != null) {
                totalExercisesLabel.setText(String.valueOf(allExercises != null ? allExercises.size() : 0));
            }
            if (totalEquipmentLabel != null) {
                totalEquipmentLabel.setText(String.valueOf(allEquipment != null ? allEquipment.size() : 0));
            }
            if (totalMuscleGroupsLabel != null) {
                totalMuscleGroupsLabel.setText(String.valueOf(allMuscleGroups != null ? allMuscleGroups.size() : 0));
            }
        } catch (Exception e) {
            System.err.println("âŒ Stats update error: " + e.getMessage());
        }
    }

    private void populateFilters() {
        try {
            // Populate muscle group filter
            if (muscleGroupFilter != null && allMuscleGroups != null) {
                ObservableList<String> muscleGroupNames = FXCollections.observableArrayList();
                muscleGroupNames.add("TÃ¼mÃ¼");
                allMuscleGroups.forEach(mg -> muscleGroupNames.add(mg.getName()));
                muscleGroupFilter.setItems(muscleGroupNames);
            }

            // Populate equipment filter
            if (equipmentFilter != null && allEquipment != null) {
                ObservableList<String> equipmentNames = FXCollections.observableArrayList();
                equipmentNames.add("TÃ¼mÃ¼");
                allEquipment.forEach(eq -> equipmentNames.add(eq.getName()));
                equipmentFilter.setItems(equipmentNames);
            }
            
            System.out.println("âœ… Filters populated");
        } catch (Exception e) {
            System.err.println("âŒ Filter population error: " + e.getMessage());
        }
    }

    private void setLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(isLoading);
        }
    }

    // Tab Navigation Methods
    @FXML
    private void showExercisesTab() {
        currentTab = "exercises";
        updateTabVisibility();
        updateTabButtons();
        System.out.println("âœ… Switched to exercises tab");
    }
    
    @FXML
    private void showEquipmentTab() {
        currentTab = "equipment";
        updateTabVisibility();
        updateTabButtons();
        System.out.println("âœ… Switched to equipment tab");
    }

    @FXML
    private void showMuscleGroupsTab() {
        currentTab = "muscleGroups";
        updateTabVisibility();
        updateTabButtons();
        System.out.println("âœ… Switched to muscle groups tab");
    }

    private void updateTabVisibility() {
        if (exercisesTabContent != null) {
            exercisesTabContent.setVisible("exercises".equals(currentTab));
        }
        if (equipmentTabContent != null) {
            equipmentTabContent.setVisible("equipment".equals(currentTab));
        }
        if (muscleGroupsTabContent != null) {
            muscleGroupsTabContent.setVisible("muscleGroups".equals(currentTab));
        }
    }

    private void updateTabButtons() {
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10px 10px 0 0; -fx-border-width: 0; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: rgba(52,152,219,0.3); -fx-text-fill: #3498db; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10px 10px 0 0; -fx-border-width: 0; -fx-cursor: hand;";

        if (exercisesTabBtn != null) {
            exercisesTabBtn.setStyle("exercises".equals(currentTab) ? activeStyle : inactiveStyle);
        }
        if (equipmentTabBtn != null) {
            equipmentTabBtn.setStyle("equipment".equals(currentTab) ? activeStyle : inactiveStyle);
        }
        if (muscleGroupsTabBtn != null) {
            muscleGroupsTabBtn.setStyle("muscleGroups".equals(currentTab) ? activeStyle : inactiveStyle);
        }
    }

    // Filter Methods
    private void filterExercises() {
        if (allExercises == null) return;
        
        String searchText = exerciseSearchField != null ? exerciseSearchField.getText().toLowerCase() : "";
        String selectedMuscleGroup = muscleGroupFilter != null ? muscleGroupFilter.getValue() : "TÃ¼mÃ¼";
        String selectedEquipment = equipmentFilter != null ? equipmentFilter.getValue() : "TÃ¼mÃ¼";
        String selectedDifficulty = difficultyFilter != null ? difficultyFilter.getValue() : "TÃ¼mÃ¼";
        
        List<Exercise> filtered = allExercises.stream()
            .filter(exercise -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    boolean nameMatch = exercise.getName().toLowerCase().contains(searchText);
                    boolean descMatch = exercise.getDescription() != null && exercise.getDescription().toLowerCase().contains(searchText);
                    if (!nameMatch && !descMatch) return false;
                }
                
                // Muscle group filter
                if (!"TÃ¼mÃ¼".equals(selectedMuscleGroup)) {
                    boolean muscleMatch = exercise.getMuscleGroups() != null && 
                        exercise.getMuscleGroups().stream().anyMatch(mg -> mg.getName().equals(selectedMuscleGroup));
                    if (!muscleMatch) return false;
                }
                
                // Equipment filter
                if (!"TÃ¼mÃ¼".equals(selectedEquipment)) {
                    boolean equipmentMatch = exercise.getEquipments() != null && 
                        exercise.getEquipments().stream().anyMatch(eq -> eq.getName().equals(selectedEquipment));
                    if (!equipmentMatch) return false;
                }
                
                // Difficulty filter
                if (!"TÃ¼mÃ¼".equals(selectedDifficulty)) {
                    if (!selectedDifficulty.equals(exercise.getDifficultyLevel())) return false;
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        exerciseList.clear();
        exerciseList.addAll(filtered);
        
        System.out.println("ğŸ” Exercises filtered: " + filtered.size() + " results");
    }

    private void filterEquipment() {
        if (allEquipment == null) return;
        
        String searchText = equipmentSearchField != null ? equipmentSearchField.getText().toLowerCase() : "";
        
        List<Equipment> filtered = allEquipment.stream()
            .filter(equipment -> {
                if (!searchText.isEmpty()) {
                    boolean nameMatch = equipment.getName().toLowerCase().contains(searchText);
                    boolean descMatch = equipment.getDescription() != null && equipment.getDescription().toLowerCase().contains(searchText);
                    return nameMatch || descMatch;
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        equipmentList.clear();
        equipmentList.addAll(filtered);
        
        System.out.println("ğŸ” Equipment filtered: " + filtered.size() + " results");
    }

    private void filterMuscleGroups() {
        if (allMuscleGroups == null) return;
        
        String searchText = muscleGroupSearchField != null ? muscleGroupSearchField.getText().toLowerCase() : "";
        
        List<MuscleGroup> filtered = allMuscleGroups.stream()
            .filter(muscleGroup -> {
                if (!searchText.isEmpty()) {
                    boolean nameMatch = muscleGroup.getName().toLowerCase().contains(searchText);
                    boolean descMatch = muscleGroup.getDescription() != null && muscleGroup.getDescription().toLowerCase().contains(searchText);
                    return nameMatch || descMatch;
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        muscleGroupList.clear();
        muscleGroupList.addAll(filtered);
        
        System.out.println("ğŸ” Muscle groups filtered: " + filtered.size() + " results");
    }

    // Action Handlers
    @FXML
    private void handleAddExercise() {
        showExerciseDialog(null);
    }
    
    @FXML
    private void handleRefreshExercises() {
        loadData();
    }
    
    @FXML
    private void handleClearFilters() {
        if (exerciseSearchField != null) exerciseSearchField.clear();
        if (muscleGroupFilter != null) muscleGroupFilter.setValue("TÃ¼mÃ¼");
        if (equipmentFilter != null) equipmentFilter.setValue("TÃ¼mÃ¼");
        if (difficultyFilter != null) difficultyFilter.setValue("TÃ¼mÃ¼");
        filterExercises();
    }

    @FXML
    private void handleAddEquipment() {
        showEquipmentDialog(null);
    }

    @FXML
    private void handleRefreshEquipment() {
        loadData();
    }
    
    @FXML
    private void handleAddMuscleGroup() {
        showMuscleGroupDialog(null);
    }
    
    @FXML
    private void handleRefreshMuscleGroups() {
        loadData();
    }

    private void handleEditExercise(Exercise exercise) {
        showExerciseDialog(exercise);
    }

    private void handleDeleteExercise(Exercise exercise) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Egzersiz Sil");
        alert.setHeaderText(null);
        alert.setContentText("'" + exercise.getName() + "' egzersizini silmek istediÄŸinizden emin misiniz?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = exerciseService.deleteExercise(exercise.getId());
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Egzersiz baÅŸarÄ±yla silindi!");
                        loadData(); // Refresh data
                    } else {
                        showAlert("Hata", "Egzersiz silinirken hata oluÅŸtu!");
                    }
                } catch (Exception e) {
                    showAlert("Hata", "Egzersiz silinirken hata oluÅŸtu: " + e.getMessage());
                }
            }
        });
    }

    private void handleEditEquipment(Equipment equipment) {
        showEquipmentDialog(equipment);
    }

    private void handleDeleteEquipment(Equipment equipment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ekipman Sil");
        alert.setHeaderText(null);
        alert.setContentText("'" + equipment.getName() + "' ekipmanÄ±nÄ± silmek istediÄŸinizden emin misiniz?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = equipmentService.deleteEquipment(equipment.getId());
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla silindi!");
                        loadData(); // Refresh data
                    } else {
                        showAlert("Hata", "Ekipman silinirken hata oluÅŸtu!");
                    }
                } catch (Exception e) {
                    showAlert("Hata", "Ekipman silinirken hata oluÅŸtu: " + e.getMessage());
                }
            }
        });
    }

    private void handleEditMuscleGroup(MuscleGroup muscleGroup) {
        showMuscleGroupDialog(muscleGroup);
    }

    private void handleDeleteMuscleGroup(MuscleGroup muscleGroup) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kas Grubu Sil");
        alert.setHeaderText(null);
        alert.setContentText("'" + muscleGroup.getName() + "' kas grubunu silmek istediÄŸinizden emin misiniz?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = muscleGroupService.deleteMuscleGroup(muscleGroup.getId());
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla silindi!");
                        loadData(); // Refresh data
                    } else {
                        showAlert("Hata", "Kas grubu silinirken hata oluÅŸtu!");
                    }
                } catch (Exception e) {
                    showAlert("Hata", "Kas grubu silinirken hata oluÅŸtu: " + e.getMessage());
                }
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("âœ… ExerciseManagementContent: Current user set to " + (user != null ? user.getUsername() : "null"));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Dialog Methods
    private void showExerciseDialog(Exercise exercise) {
        Dialog<Exercise> dialog = new Dialog<>();
        dialog.setTitle(exercise == null ? "ğŸ‹ Yeni Egzersiz OluÅŸtur" : "âœï¸ Egzersiz DÃ¼zenle");
        dialog.setHeaderText("Egzersiz bilgilerini girin ve gerekli kas grubu ile ekipmanÄ± seÃ§in");

        ButtonType saveButtonType = new ButtonType("ğŸ’¾ Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Egzersiz adÄ± (Ã¶rn: Bench Press)");
        if (exercise != null) nameField.setText(exercise.getName());

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Egzersizin kÄ±sa aÃ§Ä±klamasÄ±");
        descriptionArea.setPrefRowCount(2);
        descriptionArea.setWrapText(true);
        if (exercise != null && exercise.getDescription() != null) {
            descriptionArea.setText(exercise.getDescription());
        }

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.setItems(FXCollections.observableArrayList("BaÅŸlangÄ±Ã§", "Orta", "Ä°leri", "Uzman"));
        difficultyCombo.setPromptText("Zorluk seviyesi seÃ§in");
        if (exercise != null) difficultyCombo.setValue(exercise.getDifficultyLevel());

        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("DetaylÄ± egzersiz talimatlarÄ± ve teknik ipuÃ§larÄ±");
        instructionsArea.setPrefRowCount(3);
        instructionsArea.setWrapText(true);
        if (exercise != null && exercise.getInstructions() != null) {
            instructionsArea.setText(exercise.getInstructions());
        }

        // Kas Grubu SeÃ§imi - Ã‡oklu seÃ§im
        ListView<MuscleGroup> muscleGroupList = new ListView<>();
        muscleGroupList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        muscleGroupList.setPrefHeight(100);
        
        // Ekipman SeÃ§imi - Ã‡oklu seÃ§im
        ListView<Equipment> equipmentList = new ListView<>();
        equipmentList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        equipmentList.setPrefHeight(100);

        // Mevcut verileri yÃ¼kle
        loadMuscleGroupsAndEquipments(muscleGroupList, equipmentList, exercise);

        // Layout
        var grid = new javafx.scene.layout.GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(25, 25, 25, 25));

        // Form elemanlarÄ±
        grid.add(new Label("ğŸ“ Egzersiz AdÄ±: *"), 0, 0);
        grid.add(nameField, 1, 0, 2, 1);
        
        grid.add(new Label("ğŸ“„ AÃ§Ä±klama:"), 0, 1);
        grid.add(descriptionArea, 1, 1, 2, 1);
        
        grid.add(new Label("ğŸ“Š Zorluk Seviyesi: *"), 0, 2);
        grid.add(difficultyCombo, 1, 2);
        
        grid.add(new Label("ğŸ’ª Hedef Kas GruplarÄ±: *"), 0, 3);
        grid.add(new VBox(5, 
            new Label("(Birden fazla seÃ§ebilirsiniz)"), 
            muscleGroupList), 1, 3, 2, 1);
        
        grid.add(new Label("ğŸ”§ Gerekli Ekipmanlar: *"), 0, 4);
        grid.add(new VBox(5, 
            new Label("(Birden fazla seÃ§ebilirsiniz)"), 
            equipmentList), 1, 4, 2, 1);
        
        grid.add(new Label("ğŸ“‹ Talimatlar:"), 0, 5);
        grid.add(instructionsArea, 1, 5, 2, 1);

        // Grid sÃ¼tun geniÅŸlikleri
        grid.getColumnConstraints().addAll(
            new javafx.scene.layout.ColumnConstraints(150),
            new javafx.scene.layout.ColumnConstraints(250),
            new javafx.scene.layout.ColumnConstraints(250)
        );

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(700, 650);

        // Save button validation
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validation logic
        Runnable validateForm = () -> {
            boolean isValid = !nameField.getText().trim().isEmpty() &&
                            difficultyCombo.getValue() != null &&
                            !muscleGroupList.getSelectionModel().getSelectedItems().isEmpty() &&
                            !equipmentList.getSelectionModel().getSelectedItems().isEmpty();
            saveButton.setDisable(!isValid);
        };

        nameField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        difficultyCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        muscleGroupList.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<MuscleGroup>) c -> validateForm.run());
        equipmentList.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<Equipment>) c -> validateForm.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Exercise result = exercise == null ? new Exercise() : exercise;
                result.setName(nameField.getText().trim());
                result.setDescription(descriptionArea.getText().trim());
                result.setDifficultyLevel(difficultyCombo.getValue());
                result.setInstructions(instructionsArea.getText().trim());
                
                // SeÃ§ilen kas gruplarÄ±nÄ± ekle
                var selectedMuscleGroups = new java.util.ArrayList<>(muscleGroupList.getSelectionModel().getSelectedItems());
                result.setMuscleGroups(selectedMuscleGroups);
                
                // SeÃ§ilen ekipmanlarÄ± ekle
                var selectedEquipments = new java.util.ArrayList<>(equipmentList.getSelectionModel().getSelectedItems());
                result.setEquipments(selectedEquipments);
                
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            saveExercise(result, exercise == null);
        });
    }

    private void showEquipmentDialog(Equipment equipment) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle(equipment == null ? "Yeni Ekipman" : "Ekipman DÃ¼zenle");
        dialog.setHeaderText("Ekipman bilgilerini girin");

        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Ekipman adÄ±");
        if (equipment != null) nameField.setText(equipment.getName());

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("AÃ§Ä±klama");
        descriptionArea.setPrefRowCount(3);
        if (equipment != null && equipment.getDescription() != null) {
            descriptionArea.setText(equipment.getDescription());
        }

        var grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Ekipman AdÄ±:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("AÃ§Ä±klama:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable save button
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Equipment result = equipment == null ? new Equipment() : equipment;
                result.setName(nameField.getText().trim());
                result.setDescription(descriptionArea.getText().trim());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            saveEquipment(result, equipment == null);
        });
    }

    private void showMuscleGroupDialog(MuscleGroup muscleGroup) {
        Dialog<MuscleGroup> dialog = new Dialog<>();
        dialog.setTitle(muscleGroup == null ? "Yeni Kas Grubu" : "Kas Grubu DÃ¼zenle");
        dialog.setHeaderText("Kas grubu bilgilerini girin");

        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Kas grubu adÄ±");
        if (muscleGroup != null) nameField.setText(muscleGroup.getName());

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("AÃ§Ä±klama");
        descriptionArea.setPrefRowCount(3);
        if (muscleGroup != null && muscleGroup.getDescription() != null) {
            descriptionArea.setText(muscleGroup.getDescription());
        }

        var grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Kas Grubu AdÄ±:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("AÃ§Ä±klama:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable save button
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                MuscleGroup result = muscleGroup == null ? new MuscleGroup() : muscleGroup;
                result.setName(nameField.getText().trim());
                result.setDescription(descriptionArea.getText().trim());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            saveMuscleGroup(result, muscleGroup == null);
        });
    }

    // Save Methods
    private void saveExercise(Exercise exercise, boolean isNew) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Kas grubu ID'lerini topla
                List<Integer> muscleGroupIds = null;
                if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                    muscleGroupIds = new java.util.ArrayList<>();
                    for (MuscleGroup mg : exercise.getMuscleGroups()) {
                        muscleGroupIds.add(mg.getId());
                    }
                }
                
                // Ekipman ID'lerini topla
                List<Integer> equipmentIds = null;
                if (exercise.getEquipments() != null && !exercise.getEquipments().isEmpty()) {
                    equipmentIds = new java.util.ArrayList<>();
                    for (Equipment eq : exercise.getEquipments()) {
                        equipmentIds.add(eq.getId());
                    }
                }
                
                if (isNew) {
                    return exerciseService.createExercise(
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getDifficultyLevel(),
                        exercise.getInstructions(),
                        muscleGroupIds,
                        equipmentIds
                    );
                } else {
                    return exerciseService.updateExercise(
                        exercise.getId(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getDifficultyLevel(),
                        exercise.getInstructions(),
                        muscleGroupIds,
                        equipmentIds
                    );
                }
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (task.getValue()) {
                    showAlert("BaÅŸarÄ±lÄ±", (isNew ? "Egzersiz baÅŸarÄ±yla eklendi!" : "Egzersiz baÅŸarÄ±yla gÃ¼ncellendi!"));
                    loadData();
                } else {
                    showAlert("Hata", "Egzersiz kaydedilemedi!");
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Egzersiz kaydedilirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void saveEquipment(Equipment equipment, boolean isNew) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (isNew) {
                    Equipment created = equipmentService.createEquipment(equipment);
                    return created != null;
                } else {
                    return equipmentService.updateEquipment(equipment);
                }
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (task.getValue()) {
                    showAlert("BaÅŸarÄ±lÄ±", (isNew ? "Ekipman baÅŸarÄ±yla eklendi!" : "Ekipman baÅŸarÄ±yla gÃ¼ncellendi!"));
                    loadData();
                } else {
                    showAlert("Hata", "Ekipman kaydedilemedi!");
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Ekipman kaydedilirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void saveMuscleGroup(MuscleGroup muscleGroup, boolean isNew) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (isNew) {
                    MuscleGroup created = muscleGroupService.createMuscleGroup(muscleGroup);
                    return created != null;
                } else {
                    return muscleGroupService.updateMuscleGroup(muscleGroup);
                }
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (task.getValue()) {
                    showAlert("BaÅŸarÄ±lÄ±", (isNew ? "Kas grubu baÅŸarÄ±yla eklendi!" : "Kas grubu baÅŸarÄ±yla gÃ¼ncellendi!"));
                    loadData();
                } else {
                    showAlert("Hata", "Kas grubu kaydedilemedi!");
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Kas grubu kaydedilirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Helper Methods
    private void loadMuscleGroupsAndEquipments(ListView<MuscleGroup> muscleGroupList, ListView<Equipment> equipmentList, Exercise exercise) {
        try {
            // Kas gruplarÄ±nÄ± yÃ¼kle
            var muscleGroups = muscleGroupService.getAllMuscleGroups();
            muscleGroupList.setItems(FXCollections.observableArrayList(muscleGroups));
            
            // Mevcut egzersizin kas gruplarÄ±nÄ± seÃ§
            if (exercise != null && exercise.getMuscleGroups() != null) {
                List<Integer> selectedIndices = new ArrayList<>();
                for (int i = 0; i < muscleGroups.size(); i++) {
                    MuscleGroup mg = muscleGroups.get(i);
                    for (MuscleGroup exerciseMg : exercise.getMuscleGroups()) {
                        if (exerciseMg.getId() == mg.getId()) {
                            selectedIndices.add(i);
                            break;
                        }
                    }
                }
                
                // SeÃ§ilen tÃ¼m indeksleri tek seferde ayarla
                if (!selectedIndices.isEmpty()) {
                    int[] indices = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
                    muscleGroupList.getSelectionModel().selectIndices(indices[0], indices);
                }
            }
            
            // EkipmanlarÄ± yÃ¼kle
            var equipments = equipmentService.getAllEquipment();
            equipmentList.setItems(FXCollections.observableArrayList(equipments));
            
            // Mevcut egzersizin ekipmanlarÄ±nÄ± seÃ§
            if (exercise != null && exercise.getEquipments() != null) {
                List<Integer> selectedIndices = new ArrayList<>();
                for (int i = 0; i < equipments.size(); i++) {
                    Equipment eq = equipments.get(i);
                    for (Equipment exerciseEq : exercise.getEquipments()) {
                        if (exerciseEq.getId() == eq.getId()) {
                            selectedIndices.add(i);
                            break;
                        }
                    }
                }
                
                // SeÃ§ilen tÃ¼m indeksleri tek seferde ayarla
                if (!selectedIndices.isEmpty()) {
                    int[] indices = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
                    equipmentList.getSelectionModel().selectIndices(indices[0], indices);
                }
            }
            
            // ListView'larÄ± daha kullanÄ±cÄ± dostu hale getir
            muscleGroupList.setCellFactory(listView -> new ListCell<MuscleGroup>() {
                @Override
                protected void updateItem(MuscleGroup item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ğŸ’ª " + item.getName());
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            setTooltip(new Tooltip(item.getDescription()));
                        }
                    }
                }
            });
            
            equipmentList.setCellFactory(listView -> new ListCell<Equipment>() {
                @Override
                protected void updateItem(Equipment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ğŸ”§ " + item.getName());
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            setTooltip(new Tooltip(item.getDescription()));
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Kas grubu/ekipman yÃ¼kleme hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Kas gruplarÄ± ve ekipmanlar yÃ¼klenirken hata oluÅŸtu: " + e.getMessage());
        }
    }
} 



