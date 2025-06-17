package com.example.selfworkout.controller;

import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.User;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.service.ExerciseService;
import com.example.selfworkout.service.MuscleGroupService;
import com.example.selfworkout.service.EquipmentService;
import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.util.SceneManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Egzersiz YÃ¶netimi ekranÄ±nÄ±n controller sÄ±nÄ±fÄ±
 * Admin kullanÄ±cÄ±larÄ± iÃ§in egzersiz CRUD iÅŸlemlerini yÃ¶netir
 */
public class ExerciseManagementController extends BaseController {

    // Navigasyon ButonlarÄ±
    @FXML private Button dashboardNavButton;
    @FXML private Button userManagementNavButton;
    @FXML private Button exerciseManagementNavButton;
    @FXML private Button systemReportsNavButton;
    @FXML private Button databaseManagementNavButton;
    @FXML private Button systemSettingsNavButton;
    @FXML private Button switchToUserNavButton;
    @FXML private Button logoutButton;

    // KullanÄ±cÄ± Bilgi AlanlarÄ±
    @FXML private Label sidebarWelcomeLabel;
    @FXML private Label sidebarUserInfoLabel;

    // Kontrol AlanlarÄ±
    @FXML private Label exerciseCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private Button clearFiltersButton;
    @FXML private Button addExerciseButton;
    @FXML private Button refreshButton;

    // Kart Konteyneri
    @FXML private FlowPane exerciseCardsContainer;

    // YÃ¼kleme GÃ¶stergesi
    @FXML private ProgressIndicator loadingIndicator;

    // Tab Paneli
    @FXML private TabPane mainTabPane;
    @FXML private Tab exercisesTab;
    @FXML private Tab muscleGroupsTab;
    @FXML private Tab equipmentsTab;

    // Kas GruplarÄ± Tab Kontrolleri
    @FXML private TextField muscleGroupSearchField;
    @FXML private Button clearMuscleGroupFiltersButton;
    @FXML private Button addMuscleGroupButton;
    @FXML private Button refreshMuscleGroupsButton;
    @FXML private Label muscleGroupCountLabel;
    @FXML private ProgressIndicator muscleGroupLoadingIndicator;
    @FXML private TableView<MuscleGroup> muscleGroupsTable;
    @FXML private TableColumn<MuscleGroup, String> muscleGroupNameColumn;
    @FXML private TableColumn<MuscleGroup, String> muscleGroupDescriptionColumn;
    @FXML private TableColumn<MuscleGroup, Integer> muscleGroupExerciseCountColumn;
    @FXML private TableColumn<MuscleGroup, Void> muscleGroupActionsColumn;

    // Ekipmanlar Tab Kontrolleri
    @FXML private TextField equipmentSearchField;
    @FXML private Button clearEquipmentFiltersButton;
    @FXML private Button addEquipmentButton;
    @FXML private Button refreshEquipmentsButton;
    @FXML private Label equipmentCountLabel;
    @FXML private ProgressIndicator equipmentLoadingIndicator;
    @FXML private TableView<Equipment> equipmentsTable;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, String> equipmentDescriptionColumn;
    @FXML private TableColumn<Equipment, Integer> equipmentExerciseCountColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionsColumn;

    // Servisler ve Veriler
    private ExerciseService exerciseService;
    private MuscleGroupService muscleGroupService;
    private EquipmentService equipmentService;

    private ObservableList<Exercise> allExercises = FXCollections.observableArrayList();
    private FilteredList<Exercise> filteredExercises;
    
    private ObservableList<MuscleGroup> allMuscleGroups = FXCollections.observableArrayList();
    private FilteredList<MuscleGroup> filteredMuscleGroups;
    
    private ObservableList<Equipment> allEquipments = FXCollections.observableArrayList();
    private FilteredList<Equipment> filteredEquipments;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }
    
    @Override
    protected void initializeController() {
        initializeServices();
        setupUI();
        setupMuscleGroupTable();
        setupEquipmentTable();
        setupEventListeners();
        setupUserInfo();
        loadAllData();
    }

    private void initializeServices() {
        try {
            // BaseController'dan gelen serviceManager'ı kullan
            exerciseService = serviceManager.getExerciseService();
            muscleGroupService = serviceManager.getMuscleGroupService();
            equipmentService = serviceManager.getEquipmentService();
        } catch (Exception e) {
            System.err.println("Servis baÅŸlatma hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Servisler baÅŸlatÄ±lÄ±rken bir hata oluÅŸtu: " + e.getMessage());
        }
    }

    private void setupUI() {
        // Egzersiz kartlarÄ± iÃ§in filtreli liste baÄŸlantÄ±sÄ±
        filteredExercises = new FilteredList<>(allExercises, p -> true);

        // Kas gruplarÄ± tablosu ile filtreli liste baÄŸlantÄ±sÄ±
        if (muscleGroupsTable != null) {
            filteredMuscleGroups = new FilteredList<>(allMuscleGroups, p -> true);
            muscleGroupsTable.setItems(filteredMuscleGroups);
        }

        // Ekipmanlar tablosu ile filtreli liste baÄŸlantÄ±sÄ±
        if (equipmentsTable != null) {
            filteredEquipments = new FilteredList<>(allEquipments, p -> true);
            equipmentsTable.setItems(filteredEquipments);
        }

        // ComboBox default deÄŸerleri
        if (difficultyFilter != null) {
            difficultyFilter.setValue("Tumu");
        }

        // Loading indicator baÅŸlangÄ±Ã§ta gizli
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        if (muscleGroupLoadingIndicator != null) {
            muscleGroupLoadingIndicator.setVisible(false);
        }
        if (equipmentLoadingIndicator != null) {
            equipmentLoadingIndicator.setVisible(false);
        }
        
        // Egzersiz kartlarÄ±nÄ± gÃ¶ster
        updateExerciseCards();
    }

    private void updateExerciseCards() {
        if (exerciseCardsContainer == null) return;
        
        // Ã–nce mevcut kartlarÄ± temizle
        exerciseCardsContainer.getChildren().clear();
        
        // FiltrelenmiÅŸ egzersizler iÃ§in kartlar oluÅŸtur
        for (Exercise exercise : filteredExercises) {
            VBox card = createExerciseCard(exercise);
            exerciseCardsContainer.getChildren().add(card);
        }
    }
    
    private VBox createExerciseCard(Exercise exercise) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                     "-fx-border-radius: 12px; -fx-cursor: hand;");
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        
        // BaÅŸlÄ±k
        String exerciseName = exercise.getName();
        // BaÅŸlÄ±k
        Label titleLabel = new Label(exercise.getName());
        titleLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: bold; " +
                          "-fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        
        // AÃ§Ä±klama
        String description = exercise.getDescription() != null ? exercise.getDescription() : "AÃ§Ä±klama yok";
        if (description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #7f8c8d; " +
                          "-fx-wrap-text: true;");
        
        // Zorluk seviyesi
        Label difficultyLabel = new Label("Zorluk: " + getDifficultyDisplayText(exercise.getDifficultyLevel()));
        difficultyLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: 500; " +
                                getDifficultyStyle(exercise.getDifficultyLevel()));
        
        // Kas gruplarÄ±
        String muscleGroupsText = exercise.getMuscleGroups() == null || exercise.getMuscleGroups().isEmpty() ? 
            "BelirtilmemiÅŸ" : 
            exercise.getMuscleGroups().stream()
                .map(MuscleGroup::getName)
                .collect(Collectors.joining(", "));
        Label muscleGroupsLabel = new Label("Kas GruplarÄ±: " + muscleGroupsText);
        muscleGroupsLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #34495e; " +
                                 "-fx-wrap-text: true;");
        
        // Ekipmanlar
        String equipmentsText = exercise.getEquipments() == null || exercise.getEquipments().isEmpty() ? 
            "Ekipman yok" : 
            exercise.getEquipments().stream()
                .map(Equipment::getName)
                .collect(Collectors.joining(", "));
        Label equipmentsLabel = new Label("Ekipmanlar: " + equipmentsText);
        equipmentsLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #34495e; " +
                               "-fx-wrap-text: true;");
        
        // Ä°ÅŸlem butonlarÄ±
        HBox actionBox = new HBox(10);
        actionBox.setStyle("-fx-alignment: center;");
        
        Button editButton = new Button("DÃ¼zenle");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                          "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; " +
                          "-fx-font-family: 'Segoe UI', 'System', 'Arial Unicode MS'; -fx-font-weight: 500;");
        editButton.setOnAction(e -> handleEditExercise(exercise));
        
        Button deleteButton = new Button("Sil");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                            "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand; " +
                            "-fx-font-family: 'Segoe UI', 'System', 'Arial Unicode MS'; -fx-font-weight: 500;");
        deleteButton.setOnAction(e -> handleDeleteExercise(exercise));
        
        actionBox.getChildren().addAll(editButton, deleteButton);
        
        card.getChildren().addAll(titleLabel, descLabel, difficultyLabel, muscleGroupsLabel, equipmentsLabel, actionBox);
        
        return card;
    }



    private void setupEventListeners() {
        // Egzersiz arama alanÄ± dinleyicisi
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }

        // Zorluk seviyesi filtresi dinleyicisi
        if (difficultyFilter != null) {
            difficultyFilter.setOnAction(e -> applyFilters());
        }

        // Kas grubu arama alanÄ± dinleyicisi
        if (muscleGroupSearchField != null) {
            muscleGroupSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyMuscleGroupFilters();
            });
        }

        // Ekipman arama alanÄ± dinleyicisi
        if (equipmentSearchField != null) {
            equipmentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyEquipmentFilters();
            });
        }
    }

    private void applyFilters() {
        filteredExercises.setPredicate(exercise -> {
            // Arama filtresi
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!exercise.getName().toLowerCase().contains(lowerCaseFilter) &&
                    (exercise.getDescription() == null || !exercise.getDescription().toLowerCase().contains(lowerCaseFilter))) {
                    return false;
                }
            }

            // Zorluk seviyesi filtresi
            String difficultyFilterValue = difficultyFilter.getValue();
            if (difficultyFilterValue != null && !difficultyFilterValue.equals("Tumu")) {
                String filterLevel = mapDisplayToActualDifficulty(difficultyFilterValue);
                if (exercise.getDifficultyLevel() == null || !exercise.getDifficultyLevel().equalsIgnoreCase(filterLevel)) {
                    return false;
                }
            }

            return true;
        });

        // Egzersiz sayÄ±sÄ±nÄ± gÃ¼ncelle
        updateExerciseCount();
        
        // KartlarÄ± gÃ¼ncelle
        updateExerciseCards();
    }

    private void updateExerciseCount() {
        int totalExercises = allExercises.size();
        int filteredCount = filteredExercises.size();

        if (totalExercises == filteredCount) {
            exerciseCountLabel.setText("Toplam: " + totalExercises + " egzersiz");
        } else {
            exerciseCountLabel.setText("GÃ¶sterilen: " + filteredCount + " / " + totalExercises + " egzersiz");
        }
    }

    private void setupUserInfo() {
        if (currentUser != null) {
            sidebarWelcomeLabel.setText("HoÅŸ Geldiniz");
            sidebarUserInfoLabel.setText(currentUser.getUsername());
        } else {
            sidebarWelcomeLabel.setText("HoÅŸ Geldiniz");
            sidebarUserInfoLabel.setText("admin");
        }
    }

    private void loadExercises() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        Task<List<Exercise>> loadExercisesTask = new Task<List<Exercise>>() {
            @Override
            protected List<Exercise> call() throws Exception {
                if (exerciseService != null) {
                    return exerciseService.getAllExercises();
                }
                return FXCollections.observableArrayList();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Exercise> exercises = getValue();
                    allExercises.clear();
                    allExercises.addAll(exercises);

                    updateExerciseCount();
                    updateExerciseCards();

                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }

                    showAlert("Hata", "Egzersizler yÃ¼klenirken bir hata oluÅŸtu: " + getException().getMessage());
                });
            }
        };

        Thread loadThread = new Thread(loadExercisesTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    // Event Handler MetodlarÄ±
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        difficultyFilter.setValue("Tumu");
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        loadExercises();
    }

    @FXML
    private void handleAddExercise() {
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Yeni Egzersiz Ekle");
            dialog.setHeaderText("Yeni egzersiz bilgilerini girin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField();
            nameField.setPromptText("Egzersiz adÄ±");
            nameField.setPrefWidth(400);
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Egzersiz aÃ§Ä±klamasÄ±");
            descriptionArea.setPrefRowCount(4);
            descriptionArea.setPrefWidth(400);
            descriptionArea.setWrapText(true);
            
            ComboBox<String> difficultyCombo = new ComboBox<>();
            difficultyCombo.getItems().addAll("Kolay", "Orta", "Zor");
            difficultyCombo.setValue("Orta");
            difficultyCombo.setPrefWidth(400);
            
            // Hedef kas grubu ComboBox'Ä± - mevcut verilerden seÃ§im
            ComboBox<MuscleGroup> targetMuscleCombo = new ComboBox<>();
            targetMuscleCombo.setPromptText("Hedef kas grubunu seÃ§in");
            targetMuscleCombo.setPrefWidth(400);
            
            // Kas gruplarÄ±nÄ± yÃ¼kle
            targetMuscleCombo.getItems().clear();
            targetMuscleCombo.getItems().add(null); // BoÅŸ seÃ§enek iÃ§in
            targetMuscleCombo.getItems().addAll(allMuscleGroups);
            targetMuscleCombo.setConverter(new StringConverter<MuscleGroup>() {
                @Override
                public String toString(MuscleGroup muscleGroup) {
                    return muscleGroup == null ? "SeÃ§iniz..." : muscleGroup.getName();
                }
                
                @Override
                public MuscleGroup fromString(String string) {
                    return allMuscleGroups.stream()
                            .filter(mg -> mg.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
            
            // Gerekli ekipman ComboBox'Ä± - mevcut verilerden seÃ§im
            ComboBox<Equipment> equipmentCombo = new ComboBox<>();
            equipmentCombo.setPromptText("Gerekli ekipmanÄ± seÃ§in (isteÄŸe baÄŸlÄ±)");
            equipmentCombo.setPrefWidth(400);
            
            // EkipmanlarÄ± yÃ¼kle
            equipmentCombo.getItems().clear();
            equipmentCombo.getItems().add(null); // BoÅŸ seÃ§enek iÃ§in
            equipmentCombo.getItems().addAll(allEquipments);
            equipmentCombo.setConverter(new StringConverter<Equipment>() {
                @Override
                public String toString(Equipment equipment) {
                    return equipment == null ? "SeÃ§iniz..." : equipment.getName();
                }
                
                @Override
                public Equipment fromString(String string) {
                    return allEquipments.stream()
                            .filter(eq -> eq.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
            
            content.getChildren().addAll(
                new Label("Egzersiz AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea,
                new Label("Zorluk Seviyesi:"),
                difficultyCombo,
                new Label("Hedef Kas Grubu:"),
                targetMuscleCombo,
                new Label("Gerekli Ekipman:"),
                equipmentCombo
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // OK button validation
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Hata", "Egzersiz adÄ± boÅŸ olamaz!");
                    event.consume();
                } else if (descriptionArea.getText().trim().isEmpty()) {
                    showAlert("Hata", "Egzersiz aÃ§Ä±klamasÄ± boÅŸ olamaz!");
                    event.consume();
                }
            });
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    addExerciseConfirmed(
                        nameField.getText().trim(),
                        descriptionArea.getText().trim(),
                        mapDisplayToActualDifficulty(difficultyCombo.getValue()),
                        targetMuscleCombo.getValue(),
                        equipmentCombo.getValue()
                    );
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Egzersiz ekleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    private void handleEditExercise(Exercise exercise) {
        if (exercise == null) return;
        
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Egzersiz DÃ¼zenle");
            dialog.setHeaderText("Egzersiz bilgilerini dÃ¼zenleyin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField(exercise.getName());
            nameField.setPromptText("Egzersiz adÄ±");
            nameField.setPrefWidth(400);
            
            TextArea descriptionArea = new TextArea(exercise.getDescription());
            descriptionArea.setPromptText("Egzersiz aÃ§Ä±klamasÄ±");
            descriptionArea.setPrefRowCount(4);
            descriptionArea.setPrefWidth(400);
            descriptionArea.setWrapText(true);
            
            ComboBox<String> difficultyCombo = new ComboBox<>();
            difficultyCombo.getItems().addAll("Kolay", "Orta", "Zor");
            difficultyCombo.setValue(getDifficultyDisplayText(exercise.getDifficultyLevel()));
            difficultyCombo.setPrefWidth(400);
            
            // Hedef kas grubu ComboBox'Ä± - mevcut verilerden seÃ§im
            ComboBox<MuscleGroup> targetMuscleCombo = new ComboBox<>();
            targetMuscleCombo.setPromptText("Hedef kas grubunu seÃ§in");
            targetMuscleCombo.setPrefWidth(400);
            
            // Kas gruplarÄ±nÄ± yÃ¼kle
            targetMuscleCombo.getItems().clear();
            targetMuscleCombo.getItems().add(null); // BoÅŸ seÃ§enek iÃ§in
            targetMuscleCombo.getItems().addAll(allMuscleGroups);
            
            // Mevcut kas grubunu seÃ§ (eÄŸer varsa)
            if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                MuscleGroup currentMuscleGroup = exercise.getMuscleGroups().get(0);
                targetMuscleCombo.setValue(currentMuscleGroup);
            }
            
            targetMuscleCombo.setConverter(new StringConverter<MuscleGroup>() {
                @Override
                public String toString(MuscleGroup muscleGroup) {
                    return muscleGroup == null ? "SeÃ§iniz..." : muscleGroup.getName();
                }
                
                @Override
                public MuscleGroup fromString(String string) {
                    return allMuscleGroups.stream()
                            .filter(mg -> mg.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
            
            // Gerekli ekipman ComboBox'Ä± - mevcut verilerden seÃ§im
            ComboBox<Equipment> equipmentCombo = new ComboBox<>();
            equipmentCombo.setPromptText("Gerekli ekipmanÄ± seÃ§in (isteÄŸe baÄŸlÄ±)");
            equipmentCombo.setPrefWidth(400);
            
            // EkipmanlarÄ± yÃ¼kle
            equipmentCombo.getItems().clear();
            equipmentCombo.getItems().add(null); // BoÅŸ seÃ§enek iÃ§in
            equipmentCombo.getItems().addAll(allEquipments);
            
            // Mevcut ekipmanÄ± seÃ§ (eÄŸer varsa)
            if (exercise.getEquipments() != null && !exercise.getEquipments().isEmpty()) {
                Equipment currentEquipment = exercise.getEquipments().get(0);
                equipmentCombo.setValue(currentEquipment);
            }
            
            equipmentCombo.setConverter(new StringConverter<Equipment>() {
                @Override
                public String toString(Equipment equipment) {
                    return equipment == null ? "SeÃ§iniz..." : equipment.getName();
                }
                
                @Override
                public Equipment fromString(String string) {
                    return allEquipments.stream()
                            .filter(eq -> eq.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
            
            TextField instructionsField = new TextField(exercise.getInstructions() != null ? exercise.getInstructions() : "");
            instructionsField.setPromptText("Talimatlar (isteÄŸe baÄŸlÄ±)");
            instructionsField.setPrefWidth(400);
            
            content.getChildren().addAll(
                new Label("Egzersiz AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea,
                new Label("Zorluk Seviyesi:"),
                difficultyCombo,
                new Label("Hedef Kas Grubu:"),
                targetMuscleCombo,
                new Label("Gerekli Ekipman:"),
                equipmentCombo,
                new Label("Talimatlar:"),
                instructionsField
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // OK button validation
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Hata", "Egzersiz adÄ± boÅŸ olamaz!");
                    event.consume();
                } else if (descriptionArea.getText().trim().isEmpty()) {
                    showAlert("Hata", "Egzersiz aÃ§Ä±klamasÄ± boÅŸ olamaz!");
                    event.consume();
                }
            });
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    editExerciseConfirmed(
                        exercise.getId(),
                        nameField.getText().trim(),
                        descriptionArea.getText().trim(),
                        mapDisplayToActualDifficulty(difficultyCombo.getValue()),
                        instructionsField.getText().trim(),
                        targetMuscleCombo.getValue(),
                        equipmentCombo.getValue()
                    );
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Egzersiz dÃ¼zenleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    private void handleDeleteExercise(Exercise exercise) {
        if (exercise == null) return;

        // Onay dialog'u gÃ¶ster
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Egzersiz Silme OnayÄ±");
        confirmAlert.setHeaderText("Egzersizi silmek istediÄŸinizden emin misiniz?");
        confirmAlert.setContentText("Egzersiz: " + exercise.getName() + "\n\n" +
                                   "Bu iÅŸlem geri alÄ±namaz!");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteExerciseConfirmed(exercise);
            }
        });
    }

    private void deleteExerciseConfirmed(Exercise exercise) {
        Task<Boolean> deleteTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return exerciseService.deleteExercise(exercise.getId());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        allExercises.remove(exercise);
                        updateExerciseCount();
                        showAlert("BaÅŸarÄ±lÄ±", "Egzersiz baÅŸarÄ±yla silindi.");
                    } else {
                        showAlert("Hata", "Egzersiz silinirken bir hata oluÅŸtu.");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Egzersiz silinirken bir hata oluÅŸtu: " + getException().getMessage());
                });
            }
        };

        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }

    // Navigation Handler MetodlarÄ±
    @FXML
    private void handleDashboardNav() {
        try {
            Stage stage = (Stage) dashboardNavButton.getScene().getWindow();
            SceneManager.getInstance().switchToAdminDashboard(stage, currentUser);
        } catch (Exception e) {
            System.err.println("Dashboard'a geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dashboard'a geÃ§iÅŸ sÄ±rasÄ±nda bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleUserManagement() {
        try {
            Stage stage = (Stage) userManagementNavButton.getScene().getWindow();
            SceneManager.getInstance().switchToAdminDashboard(stage, currentUser);
        } catch (Exception e) {
            System.err.println("KullanÄ±cÄ± yÃ¶netimine geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showAlert("Hata", "KullanÄ±cÄ± yÃ¶netimine geÃ§iÅŸ sÄ±rasÄ±nda bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleSystemReports() {
        try {
            Stage stage = (Stage) systemReportsNavButton.getScene().getWindow();
            SceneManager.getInstance().switchToSystemReports(stage, currentUser);
        } catch (Exception e) {
            System.err.println("Sistem raporlarÄ±na geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Sistem raporlarÄ±na geÃ§iÅŸ sÄ±rasÄ±nda bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleDatabaseManagement() {
        showAlert("Bilgi", "ğŸ—ƒï¸ VeritabanÄ± YÃ¶netimi Ã¶zelliÄŸi yakÄ±nda eklenecek!\n\nğŸ“‹ Planlar:\nâ€¢ VeritabanÄ± yedekleme\nâ€¢ Veri silme/temizleme\nâ€¢ Performans analizi\nâ€¢ Tablo optimizasyonu");
    }

    @FXML
    private void handleSystemSettings() {
        showAlert("Bilgi", "âš™ï¸ Sistem AyarlarÄ± Ã¶zelliÄŸi yakÄ±nda eklenecek!\n\nğŸ“‹ Planlar:\nâ€¢ Tema ayarlarÄ±\nâ€¢ Bildirim ayarlarÄ±\nâ€¢ GÃ¼venlik ayarlarÄ±\nâ€¢ Uygulama konfigÃ¼rasyonu");
    }

    @FXML
    private void handleSwitchToUser() {
        try {
            Stage stage = (Stage) switchToUserNavButton.getScene().getWindow();
            SceneManager.getInstance().switchToMainDashboard(stage, currentUser);
        } catch (Exception e) {
            System.err.println("KullanÄ±cÄ± paneline geÃ§iÅŸ hatasÄ±: " + e.getMessage());
            showAlert("Hata", "KullanÄ±cÄ± paneline geÃ§iÅŸ sÄ±rasÄ±nda bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            SceneManager.getInstance().switchToLogin(stage);
        } catch (Exception e) {
            System.err.println("Ã‡Ä±kÄ±ÅŸ hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Ã‡Ä±kÄ±ÅŸ sÄ±rasÄ±nda bir hata oluÅŸtu.");
        }
    }

    // YardÄ±mcÄ± Metodlar
    private String getDifficultyDisplayText(String difficulty) {
        if (difficulty == null) return "BelirtilmemiÅŸ";
        
        switch (difficulty.toLowerCase()) {
            case "easy":
            case "kolay":
                return "Kolay";
            case "medium":
            case "orta":
                return "Orta";
            case "hard":
            case "zor":
                return "Zor";
            default:
                return difficulty;
        }
    }

    private String getDifficultyStyle(String difficulty) {
        if (difficulty == null) return "-fx-text-fill: #95a5a6;";
        
        switch (difficulty.toLowerCase()) {
            case "easy":
            case "kolay":
                return "-fx-text-fill: #27ae60; -fx-font-weight: 500;";
            case "medium":
            case "orta":
                return "-fx-text-fill: #f39c12; -fx-font-weight: 500;";
            case "hard":
            case "zor":
                return "-fx-text-fill: #e74c3c; -fx-font-weight: 500;";
            default:
                return "-fx-text-fill: #5a6c7d;";
        }
    }

    private String mapDisplayToActualDifficulty(String displayDifficulty) {
        switch (displayDifficulty) {
            case "Kolay":
                return "kolay";
            case "Orta":
                return "orta";
            case "Zor":
                return "zor";
            default:
                return displayDifficulty.toLowerCase();
        }
    }

    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        setupUserInfo();
    }
    


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==== KAS GRUPLARI TAB Ä°ÅLEMLERÄ° ====
    
    @FXML
    private void handleClearMuscleGroupFilters() {
        if (muscleGroupSearchField != null) {
            muscleGroupSearchField.clear();
        }
        applyMuscleGroupFilters();
    }

    @FXML
    private void handleAddMuscleGroup() {
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Yeni Kas Grubu Ekle");
            dialog.setHeaderText("Yeni kas grubu bilgilerini girin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField();
            nameField.setPromptText("Kas grubu adÄ± (Ã¶rn: GÃ¶ÄŸÃ¼s, SÄ±rt, Bacak)");
            nameField.setPrefWidth(300);
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Kas grubu aÃ§Ä±klamasÄ± (isteÄŸe baÄŸlÄ±)");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);
            descriptionArea.setWrapText(true);
            
            content.getChildren().addAll(
                new Label("Kas Grubu AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // OK button validation
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Hata", "Kas grubu adÄ± boÅŸ olamaz!");
                    event.consume();
                }
            });
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    addMuscleGroupConfirmed(nameField.getText().trim(), descriptionArea.getText().trim());
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Kas grubu ekleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleRefreshMuscleGroups() {
        loadMuscleGroups();
    }

    private void applyMuscleGroupFilters() {
        if (filteredMuscleGroups == null) return;
        
        filteredMuscleGroups.setPredicate(muscleGroup -> {
            if (muscleGroup == null) return false;
            
            String searchText = (muscleGroupSearchField != null) ? 
                muscleGroupSearchField.getText().toLowerCase().trim() : "";
            
            if (!searchText.isEmpty()) {
                return muscleGroup.getName().toLowerCase().contains(searchText) ||
                       (muscleGroup.getDescription() != null && 
                        muscleGroup.getDescription().toLowerCase().contains(searchText));
            }
            
            return true;
        });
        
        updateMuscleGroupCount();
    }

    private void updateMuscleGroupCount() {
        if (muscleGroupCountLabel != null) {
            int count = (filteredMuscleGroups != null) ? filteredMuscleGroups.size() : 0;
            muscleGroupCountLabel.setText("Toplam: " + count + " kas grubu");
        }
    }

    private void loadMuscleGroups() {
        if (muscleGroupService == null) return;
        
        if (muscleGroupLoadingIndicator != null) {
            muscleGroupLoadingIndicator.setVisible(true);
        }

        Task<List<MuscleGroup>> task = new Task<List<MuscleGroup>>() {
            @Override
            protected List<MuscleGroup> call() throws Exception {
                return muscleGroupService.getAllMuscleGroups();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<MuscleGroup> muscleGroups = getValue();
                    if (muscleGroups != null) {
                        allMuscleGroups.setAll(muscleGroups);
                        updateMuscleGroupCount();
                        System.out.println("âœ… " + muscleGroups.size() + " kas grubu yÃ¼klendi");
                    }
                    
                    if (muscleGroupLoadingIndicator != null) {
                        muscleGroupLoadingIndicator.setVisible(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (muscleGroupLoadingIndicator != null) {
                        muscleGroupLoadingIndicator.setVisible(false);
                    }
                    showAlert("Hata", "Kas gruplarÄ± yÃ¼klenirken bir hata oluÅŸtu: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    // ==== EKÄ°PMANLAR TAB Ä°ÅLEMLERÄ° ====
    
    @FXML
    private void handleClearEquipmentFilters() {
        if (equipmentSearchField != null) {
            equipmentSearchField.clear();
        }
        applyEquipmentFilters();
    }

    @FXML
    private void handleAddEquipment() {
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Yeni Ekipman Ekle");
            dialog.setHeaderText("Yeni ekipman bilgilerini girin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField();
            nameField.setPromptText("Ekipman adÄ± (Ã¶rn: Dumbbell, Barbell, Bench)");
            nameField.setPrefWidth(300);
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Ekipman aÃ§Ä±klamasÄ± (isteÄŸe baÄŸlÄ±)");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);
            descriptionArea.setWrapText(true);
            
            content.getChildren().addAll(
                new Label("Ekipman AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // OK button validation
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Hata", "Ekipman adÄ± boÅŸ olamaz!");
                    event.consume();
                }
            });
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    addEquipmentConfirmed(nameField.getText().trim(), descriptionArea.getText().trim());
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Ekipman ekleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    @FXML
    private void handleRefreshEquipments() {
        loadEquipments();
    }

    private void applyEquipmentFilters() {
        if (filteredEquipments == null) return;
        
        filteredEquipments.setPredicate(equipment -> {
            if (equipment == null) return false;
            
            String searchText = (equipmentSearchField != null) ? 
                equipmentSearchField.getText().toLowerCase().trim() : "";
            
            if (!searchText.isEmpty()) {
                return equipment.getName().toLowerCase().contains(searchText) ||
                       (equipment.getDescription() != null && 
                        equipment.getDescription().toLowerCase().contains(searchText));
            }
            
            return true;
        });
        
        updateEquipmentCount();
    }

    private void updateEquipmentCount() {
        if (equipmentCountLabel != null) {
            int count = (filteredEquipments != null) ? filteredEquipments.size() : 0;
            equipmentCountLabel.setText("Toplam: " + count + " ekipman");
        }
    }

    private void loadEquipments() {
        if (equipmentService == null) return;
        
        if (equipmentLoadingIndicator != null) {
            equipmentLoadingIndicator.setVisible(true);
        }

        Task<List<Equipment>> task = new Task<List<Equipment>>() {
            @Override
            protected List<Equipment> call() throws Exception {
                return equipmentService.getAllEquipment();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Equipment> equipments = getValue();
                    if (equipments != null) {
                        allEquipments.setAll(equipments);
                        updateEquipmentCount();
                        System.out.println("âœ… " + equipments.size() + " ekipman yÃ¼klendi");
                    }
                    
                    if (equipmentLoadingIndicator != null) {
                        equipmentLoadingIndicator.setVisible(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (equipmentLoadingIndicator != null) {
                        equipmentLoadingIndicator.setVisible(false);
                    }
                    showAlert("Hata", "Ekipmanlar yÃ¼klenirken bir hata oluÅŸtu: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    // ==== TABLO KURULUM METODLARÄ± ====
    
    private void setupMuscleGroupTable() {
        if (muscleGroupsTable == null) return;
        
        // Kas Grubu AdÄ±
        muscleGroupNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // AÃ§Ä±klama
        muscleGroupDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        muscleGroupDescriptionColumn.setCellFactory(column -> new TableCell<MuscleGroup, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 50) + "..." : item;
                    setText(displayText);
                    setTooltip(new Tooltip(item));
                    setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });
        
        // Egzersiz SayÄ±sÄ±
        muscleGroupExerciseCountColumn.setCellFactory(column -> new TableCell<MuscleGroup, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                    if (muscleGroup != null && muscleGroupService != null) {
                        try {
                            int exerciseCount = muscleGroupService.getExerciseCountForMuscleGroup(muscleGroup.getId());
                            setText(String.valueOf(exerciseCount));
                            
                            // Renk kodlama: 0 egzersiz kÄ±rmÄ±zÄ±, 1+ egzersiz yeÅŸil
                            if (exerciseCount == 0) {
                                setStyle("-fx-text-fill: #e74c3c; -fx-alignment: center; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #27ae60; -fx-alignment: center; -fx-font-weight: bold;");
                            }
                        } catch (Exception e) {
                            setText("?");
                            setStyle("-fx-text-fill: #f39c12; -fx-alignment: center;");
                            System.err.println("âŒ Kas grubu egzersiz sayÄ±sÄ± hesaplanÄ±rken hata: " + e.getMessage());
                        }
                    } else {
                        setText("0");
                        setStyle("-fx-text-fill: #5a6c7d; -fx-alignment: center;");
                    }
                }
            }
        });
        
        // Ä°ÅŸlemler
        muscleGroupActionsColumn.setCellFactory(column -> new TableCell<MuscleGroup, Void>() {
            private final Button editButton = new Button("âœï¸");
            private final Button deleteButton = new Button("ğŸ—‘ï¸");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                buttonsBox.setAlignment(Pos.CENTER);
                
                // DÃ¼zenle butonu
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 8px; -fx-background-radius: 3px; -fx-cursor: hand;");
                editButton.setOnAction(e -> {
                    MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                    handleEditMuscleGroup(muscleGroup);
                });
                
                // Sil butonu
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 8px; -fx-background-radius: 3px; -fx-cursor: hand;");
                deleteButton.setOnAction(e -> {
                    MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                    handleDeleteMuscleGroup(muscleGroup);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }
    
    private void setupEquipmentTable() {
        if (equipmentsTable == null) return;
        
        // Ekipman AdÄ±
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // AÃ§Ä±klama
        equipmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        equipmentDescriptionColumn.setCellFactory(column -> new TableCell<Equipment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 50) + "..." : item;
                    setText(displayText);
                    setTooltip(new Tooltip(item));
                    setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });
        
        // Egzersiz SayÄ±sÄ±
        equipmentExerciseCountColumn.setCellFactory(column -> new TableCell<Equipment, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    if (equipment != null && equipmentService != null) {
                        try {
                            int exerciseCount = equipmentService.getExerciseCountForEquipment(equipment.getId());
                            setText(String.valueOf(exerciseCount));
                            
                            // Renk kodlama: 0 egzersiz kÄ±rmÄ±zÄ±, 1+ egzersiz yeÅŸil
                            if (exerciseCount == 0) {
                                setStyle("-fx-text-fill: #e74c3c; -fx-alignment: center; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #27ae60; -fx-alignment: center; -fx-font-weight: bold;");
                            }
                        } catch (Exception e) {
                            setText("?");
                            setStyle("-fx-text-fill: #f39c12; -fx-alignment: center;");
                            System.err.println("âŒ Ekipman egzersiz sayÄ±sÄ± hesaplanÄ±rken hata: " + e.getMessage());
                        }
                    } else {
                        setText("0");
                        setStyle("-fx-text-fill: #5a6c7d; -fx-alignment: center;");
                    }
                }
            }
        });
        
        // Ä°ÅŸlemler
        equipmentActionsColumn.setCellFactory(column -> new TableCell<Equipment, Void>() {
            private final Button editButton = new Button("âœï¸");
            private final Button deleteButton = new Button("ğŸ—‘ï¸");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                buttonsBox.setAlignment(Pos.CENTER);
                
                // DÃ¼zenle butonu
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 8px; -fx-background-radius: 3px; -fx-cursor: hand;");
                editButton.setOnAction(e -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleEditEquipment(equipment);
                });
                
                // Sil butonu
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 8px; -fx-background-radius: 3px; -fx-cursor: hand;");
                deleteButton.setOnAction(e -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleDeleteEquipment(equipment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void loadAllData() {
        loadExercises();
        loadMuscleGroups();
        loadEquipments();
    }

    // ==== PLACEHOLDER METHODLAR ====
    
    private void handleEditMuscleGroup(MuscleGroup muscleGroup) {
        if (muscleGroup == null) return;
        
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Kas Grubu DÃ¼zenle");
            dialog.setHeaderText("Kas grubu bilgilerini dÃ¼zenleyin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField(muscleGroup.getName());
            nameField.setPromptText("Kas grubu adÄ±");
            nameField.setPrefWidth(300);
            
            TextArea descriptionArea = new TextArea(muscleGroup.getDescription());
            descriptionArea.setPromptText("Kas grubu aÃ§Ä±klamasÄ±");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);
            descriptionArea.setWrapText(true);
            
            content.getChildren().addAll(
                new Label("Kas Grubu AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                    editMuscleGroupConfirmed(muscleGroup.getId(), nameField.getText().trim(), descriptionArea.getText().trim());
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Kas grubu dÃ¼zenleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    private void handleDeleteMuscleGroup(MuscleGroup muscleGroup) {
        if (muscleGroup == null) return;

        // Onay dialog'u gÃ¶ster
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Kas Grubu Silme OnayÄ±");
        confirmAlert.setHeaderText("Kas grubunu silmek istediÄŸinizden emin misiniz?");
        confirmAlert.setContentText("Kas Grubu: " + muscleGroup.getName() + "\n\n" +
                                   "Bu iÅŸlem geri alÄ±namaz!");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteMuscleGroupConfirmed(muscleGroup);
            }
        });
    }

    private void handleEditEquipment(Equipment equipment) {
        if (equipment == null) return;
        
        try {
            // Dialog oluÅŸtur
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Ekipman DÃ¼zenle");
            dialog.setHeaderText("Ekipman bilgilerini dÃ¼zenleyin:");
            
            // Dialog iÃ§eriÄŸi
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            
            TextField nameField = new TextField(equipment.getName());
            nameField.setPromptText("Ekipman adÄ±");
            nameField.setPrefWidth(300);
            
            TextArea descriptionArea = new TextArea(equipment.getDescription());
            descriptionArea.setPromptText("Ekipman aÃ§Ä±klamasÄ±");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);
            descriptionArea.setWrapText(true);
            
            content.getChildren().addAll(
                new Label("Ekipman AdÄ±:"),
                nameField,
                new Label("AÃ§Ä±klama:"),
                descriptionArea
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Dialog sonucunu iÅŸle
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                    editEquipmentConfirmed(equipment.getId(), nameField.getText().trim(), descriptionArea.getText().trim());
                }
            });
            
        } catch (Exception e) {
            System.err.println("âŒ Ekipman dÃ¼zenleme dialog hatasÄ±: " + e.getMessage());
            showAlert("Hata", "Dialog aÃ§Ä±lÄ±rken bir hata oluÅŸtu.");
        }
    }

    private void handleDeleteEquipment(Equipment equipment) {
        if (equipment == null) return;

        // Onay dialog'u gÃ¶ster
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Ekipman Silme OnayÄ±");
        confirmAlert.setHeaderText("EkipmanÄ± silmek istediÄŸinizden emin misiniz?");
        confirmAlert.setContentText("Ekipman: " + equipment.getName() + "\n\n" +
                                   "Bu iÅŸlem geri alÄ±namaz!");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteEquipmentConfirmed(equipment);
            }
        });
    }
    
    private void addMuscleGroupConfirmed(String name, String description) {
        if (muscleGroupService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    MuscleGroup muscleGroup = new MuscleGroup();
                    muscleGroup.setName(name);
                    muscleGroup.setDescription(description.isEmpty() ? null : description);
                    
                    MuscleGroup savedGroup = muscleGroupService.createMuscleGroup(muscleGroup);
                    return savedGroup != null;
                } catch (Exception e) {
                    System.err.println("âŒ Kas grubu oluÅŸturma hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla eklendi: " + name);
                        loadMuscleGroups(); // Tabloyu yenile
                    } else {
                        showAlert("Hata", "Kas grubu eklenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Kas grubu ekleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void addEquipmentConfirmed(String name, String description) {
        if (equipmentService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    Equipment equipment = new Equipment();
                    equipment.setName(name);
                    equipment.setDescription(description.isEmpty() ? null : description);
                    
                    Equipment savedEquipment = equipmentService.createEquipment(equipment);
                    return savedEquipment != null;
                } catch (Exception e) {
                    System.err.println("âŒ Ekipman oluÅŸturma hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla eklendi: " + name);
                        loadEquipments(); // Tabloyu yenile
                    } else {
                        showAlert("Hata", "Ekipman eklenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Ekipman ekleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void editExerciseConfirmed(int exerciseId, String name, String description, String difficulty, String instructions, MuscleGroup targetMuscle, Equipment equipment) {
        if (exerciseService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // ExerciseService'in updateExercise metodunu Ã§aÄŸÄ±r
                    boolean success = exerciseService.updateExercise(
                        exerciseId, 
                        name, 
                        description, 
                        difficulty, 
                        instructions,
                        targetMuscle == null ? null : FXCollections.observableArrayList(targetMuscle.getId()),
                        equipment == null ? null : FXCollections.observableArrayList(equipment.getId())
                    );
                    return success;
                } catch (Exception e) {
                    System.err.println("âŒ Egzersiz gÃ¼ncelleme hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Egzersiz baÅŸarÄ±yla gÃ¼ncellendi: " + name);
                        loadExercises(); // Egzersiz listesini yenile
                    } else {
                        showAlert("Hata", "Egzersiz gÃ¼ncellenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Egzersiz gÃ¼ncelleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void editMuscleGroupConfirmed(int muscleGroupId, String name, String description) {
        if (muscleGroupService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    MuscleGroup muscleGroup = new MuscleGroup();
                    muscleGroup.setId(muscleGroupId);
                    muscleGroup.setName(name);
                    muscleGroup.setDescription(description.isEmpty() ? null : description);
                    
                    return muscleGroupService.updateMuscleGroup(muscleGroup);
                } catch (Exception e) {
                    System.err.println("âŒ Kas grubu gÃ¼ncelleme hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla gÃ¼ncellendi: " + name);
                        loadMuscleGroups();
                    } else {
                        showAlert("Hata", "Kas grubu gÃ¼ncellenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Kas grubu gÃ¼ncelleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void deleteMuscleGroupConfirmed(MuscleGroup muscleGroup) {
        if (muscleGroupService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    return muscleGroupService.deleteMuscleGroup(muscleGroup.getId());
                } catch (Exception e) {
                    System.err.println("âŒ Kas grubu silme hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla silindi: " + muscleGroup.getName());
                        allMuscleGroups.remove(muscleGroup);
                        updateMuscleGroupCount();
                    } else {
                        showAlert("Hata", "Kas grubu silinirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Kas grubu silme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void editEquipmentConfirmed(int equipmentId, String name, String description) {
        if (equipmentService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    Equipment equipment = new Equipment();
                    equipment.setId(equipmentId);
                    equipment.setName(name);
                    equipment.setDescription(description.isEmpty() ? null : description);
                    
                    return equipmentService.updateEquipment(equipment);
                } catch (Exception e) {
                    System.err.println("âŒ Ekipman gÃ¼ncelleme hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla gÃ¼ncellendi: " + name);
                        loadEquipments();
                    } else {
                        showAlert("Hata", "Ekipman gÃ¼ncellenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Ekipman gÃ¼ncelleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void deleteEquipmentConfirmed(Equipment equipment) {
        if (equipmentService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    return equipmentService.deleteEquipment(equipment.getId());
                } catch (Exception e) {
                    System.err.println("âŒ Ekipman silme hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla silindi: " + equipment.getName());
                        allEquipments.remove(equipment);
                        updateEquipmentCount();
                    } else {
                        showAlert("Hata", "Ekipman silinirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Ekipman silme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }

    private void addExerciseConfirmed(String name, String description, String difficulty, MuscleGroup targetMuscle, Equipment equipment) {
        if (exerciseService == null) return;
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // ExerciseService'in createExercise metodunu doÄŸru parametrelerle Ã§aÄŸÄ±r
                    // (name, description, difficultyLevel, instructions, muscleGroupIds, equipmentIds)
                    boolean success = exerciseService.createExercise(
                        name, 
                        description, 
                        difficulty, 
                        targetMuscle == null ? "" : "Hedef kas grubu: " + targetMuscle.getName() + 
                            (equipment == null ? "" : "\nGerekli ekipman: " + equipment.getName()), 
                        targetMuscle == null ? null : FXCollections.observableArrayList(targetMuscle.getId()),
                        equipment == null ? null : FXCollections.observableArrayList(equipment.getId())
                    );
                    return success;
                } catch (Exception e) {
                    System.err.println("âŒ Egzersiz oluÅŸturma hatasÄ±: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("BaÅŸarÄ±lÄ±", "Egzersiz baÅŸarÄ±yla eklendi: " + name);
                        loadExercises(); // Egzersiz listesini yenile
                    } else {
                        showAlert("Hata", "Egzersiz eklenirken bir hata oluÅŸtu.");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "Egzersiz ekleme iÅŸlemi baÅŸarÄ±sÄ±z: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
} 


