package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.service.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Optional;

public class MuscleGroupManagementContentController implements Initializable {

    // Stats Labels
    @FXML private Label totalMuscleGroupsLabel;
    @FXML private Label totalExercisesLabel;
    @FXML private Label popularMuscleGroupLabel;

    // Search and Filters
    @FXML private TextField searchField;

    // Action Buttons
    @FXML private Button addMuscleGroupBtn;
    @FXML private Button refreshBtn;
    @FXML private Button clearFiltersBtn;

    // Table
    @FXML private TableView<MuscleGroup> muscleGroupsTable;
    @FXML private TableColumn<MuscleGroup, Integer> idColumn;
    @FXML private TableColumn<MuscleGroup, String> nameColumn;
    @FXML private TableColumn<MuscleGroup, String> descriptionColumn;
    @FXML private TableColumn<MuscleGroup, Integer> exerciseCountColumn;
    @FXML private TableColumn<MuscleGroup, String> statusColumn;
    @FXML private TableColumn<MuscleGroup, Void> actionsColumn;

    // Loading Indicator
    @FXML private ProgressIndicator loadingIndicator;

    // Services
    private ServiceManager serviceManager;
    private User currentUser;
    private ObservableList<MuscleGroup> allMuscleGroups;
    private ObservableList<MuscleGroup> filteredMuscleGroups;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupTable();
        setupSearchField();
        loadMuscleGroups();
    }

    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            allMuscleGroups = FXCollections.observableArrayList();
            filteredMuscleGroups = FXCollections.observableArrayList();
            System.out.println("âœ… MuscleGroupManagement services initialized");
        } catch (Exception e) {
            System.err.println("âŒ Service initialization error: " + e.getMessage());
            showAlert("Hata", "Servis baÅŸlatma hatasÄ±: " + e.getMessage());
        }
    }

    private void setupTable() {
        // Column setup
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Exercise count - custom cell value factory
        exerciseCountColumn.setCellValueFactory(cellData -> {
            MuscleGroup muscleGroup = cellData.getValue();
            int count = getExerciseCountForMuscleGroup(muscleGroup.getId());
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });
        
        // Status column - always "Aktif" for now
        statusColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Aktif");
        });

        // Actions column
        setupActionsColumn();

        // Set data
        muscleGroupsTable.setItems(filteredMuscleGroups);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> {
            return new TableCell<MuscleGroup, Void>() {
                private final Button editButton = new Button("âœï¸");
                private final Button deleteButton = new Button("ğŸ—‘ï¸");

                {
                    editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");

                    editButton.setOnAction(event -> {
                        MuscleGroup muscleGroup = getTableView().getItems().get(getIndex());
                        handleEditMuscleGroup(muscleGroup);
                    });

                    deleteButton.setOnAction(event -> {
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
                        var hbox = new javafx.scene.layout.HBox(5);
                        hbox.getChildren().addAll(editButton, deleteButton);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(hbox);
                    }
                }
            };
        });
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterMuscleGroups(newValue);
        });
    }

    private void filterMuscleGroups(String searchText) {
        if (allMuscleGroups == null) return;

        filteredMuscleGroups.clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredMuscleGroups.addAll(allMuscleGroups);
        } else {
            String lowerCaseFilter = searchText.toLowerCase().trim();
            for (MuscleGroup muscleGroup : allMuscleGroups) {
                if (muscleGroup.getName().toLowerCase().contains(lowerCaseFilter) ||
                    (muscleGroup.getDescription() != null && muscleGroup.getDescription().toLowerCase().contains(lowerCaseFilter))) {
                    filteredMuscleGroups.add(muscleGroup);
                }
            }
        }
    }

    private void loadMuscleGroups() {
        setLoadingVisible(true);

        Task<List<MuscleGroup>> task = new Task<List<MuscleGroup>>() {
            @Override
            protected List<MuscleGroup> call() throws Exception {
                return serviceManager.getMuscleGroupService().getAllMuscleGroups();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    List<MuscleGroup> muscleGroups = task.getValue();
                    allMuscleGroups.clear();
                    allMuscleGroups.addAll(muscleGroups);
                    filterMuscleGroups(searchField.getText());
                    updateStats();
                    setLoadingVisible(false);
                    System.out.println("âœ… " + muscleGroups.size() + " kas grubu yÃ¼klendi");
                } catch (Exception ex) {
                    System.err.println("âŒ Kas grubu yÃ¼kleme hatasÄ±: " + ex.getMessage());
                    showAlert("Hata", "Kas gruplarÄ± yÃ¼klenirken hata oluÅŸtu");
                    setLoadingVisible(false);
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("âŒ Kas grubu yÃ¼kleme task hatasÄ±: " + task.getException().getMessage());
                showAlert("Hata", "Kas gruplarÄ± yÃ¼klenirken hata oluÅŸtu: " + task.getException().getMessage());
                setLoadingVisible(false);
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStats() {
        try {
            if (totalMuscleGroupsLabel != null) {
                totalMuscleGroupsLabel.setText(String.valueOf(allMuscleGroups != null ? allMuscleGroups.size() : 0));
            }

            // Calculate total exercises using muscle groups
            int totalExerciseCount = 0;
            String mostPopularMuscleGroup = "-";
            int maxExerciseCount = 0;

            if (allMuscleGroups != null) {
                for (MuscleGroup muscleGroup : allMuscleGroups) {
                    int exerciseCount = getExerciseCountForMuscleGroup(muscleGroup.getId());
                    totalExerciseCount += exerciseCount;
                    
                    if (exerciseCount > maxExerciseCount) {
                        maxExerciseCount = exerciseCount;
                        mostPopularMuscleGroup = muscleGroup.getName();
                    }
                }
            }

            if (totalExercisesLabel != null) {
                totalExercisesLabel.setText(String.valueOf(totalExerciseCount));
            }
            if (popularMuscleGroupLabel != null) {
                popularMuscleGroupLabel.setText(mostPopularMuscleGroup);
            }

        } catch (Exception e) {
            System.err.println("âŒ Stats gÃ¼ncelleme hatasÄ±: " + e.getMessage());
        }
    }

    private int getExerciseCountForMuscleGroup(int muscleGroupId) {
        try {
            // Get exercises that use this muscle group
            return serviceManager.getExerciseService().getExercisesByMuscleGroup(muscleGroupId).size();
        } catch (Exception e) {
            System.err.println("âŒ Exercise count alÄ±namadÄ±: " + e.getMessage());
            return 0;
        }
    }

    // Event Handlers

    @FXML
    private void handleAddMuscleGroup() {
        showMuscleGroupDialog(null);
    }

    @FXML
    private void handleRefresh() {
        loadMuscleGroups();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterMuscleGroups("");
    }

    private void handleEditMuscleGroup(MuscleGroup muscleGroup) {
        showMuscleGroupDialog(muscleGroup);
    }

    private void handleDeleteMuscleGroup(MuscleGroup muscleGroup) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kas Grubu Sil");
        alert.setHeaderText("Kas grubunu silmek istediÄŸinizden emin misiniz?");
        alert.setContentText("Kas Grubu: " + muscleGroup.getName() + "\n\nBu iÅŸlem geri alÄ±namaz!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteMuscleGroup(muscleGroup);
        }
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

        Optional<MuscleGroup> result = dialog.showAndWait();
        result.ifPresent(mg -> {
            if (muscleGroup == null) {
                saveMuscleGroup(mg);
            } else {
                updateMuscleGroup(mg);
            }
        });
    }

    private void saveMuscleGroup(MuscleGroup muscleGroup) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getMuscleGroupService().createMuscleGroup(muscleGroup);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla eklendi!");
                loadMuscleGroups();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Kas grubu eklenirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateMuscleGroup(MuscleGroup muscleGroup) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getMuscleGroupService().updateMuscleGroup(muscleGroup);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla gÃ¼ncellendi!");
                loadMuscleGroups();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Kas grubu gÃ¼ncellenirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void deleteMuscleGroup(MuscleGroup muscleGroup) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getMuscleGroupService().deleteMuscleGroup(muscleGroup.getId());
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Kas grubu baÅŸarÄ±yla silindi!");
                loadMuscleGroups();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Kas grubu silinirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Utility methods

    private void setLoadingVisible(boolean visible) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(visible);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("âœ… MuscleGroupManagement current user set: " + (user != null ? user.getUsername() : "null"));
    }
} 



