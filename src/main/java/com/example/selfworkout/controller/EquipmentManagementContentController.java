package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Equipment;
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

public class EquipmentManagementContentController implements Initializable {

    // Stats Labels
    @FXML private Label totalEquipmentLabel;
    @FXML private Label totalExercisesLabel;
    @FXML private Label popularEquipmentLabel;

    // Search and Filters
    @FXML private TextField searchField;

    // Action Buttons
    @FXML private Button addEquipmentBtn;
    @FXML private Button refreshBtn;
    @FXML private Button clearFiltersBtn;

    // Table
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Integer> idColumn;
    @FXML private TableColumn<Equipment, String> nameColumn;
    @FXML private TableColumn<Equipment, String> descriptionColumn;
    @FXML private TableColumn<Equipment, Integer> usageCountColumn;
    @FXML private TableColumn<Equipment, String> statusColumn;
    @FXML private TableColumn<Equipment, Void> actionsColumn;

    // Loading Indicator
    @FXML private ProgressIndicator loadingIndicator;

    // Services
    private ServiceManager serviceManager;
    private User currentUser;
    private ObservableList<Equipment> allEquipment;
    private ObservableList<Equipment> filteredEquipment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupTable();
        setupSearchField();
        loadEquipment();
    }

    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            allEquipment = FXCollections.observableArrayList();
            filteredEquipment = FXCollections.observableArrayList();
            System.out.println("âœ… EquipmentManagement services initialized");
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
        
        // Usage count - custom cell value factory
        usageCountColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue();
            int count = getExerciseCountForEquipment(equipment.getId());
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });
        
        // Status column - always "Aktif" for now
        statusColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Aktif");
        });

        // Actions column
        setupActionsColumn();

        // Set data
        equipmentTable.setItems(filteredEquipment);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> {
            return new TableCell<Equipment, Void>() {
                private final Button editButton = new Button("âœï¸");
                private final Button deleteButton = new Button("ğŸ—‘ï¸");

                {
                    editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");

                    editButton.setOnAction(event -> {
                        Equipment equipment = getTableView().getItems().get(getIndex());
                        handleEditEquipment(equipment);
                    });

                    deleteButton.setOnAction(event -> {
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
            filterEquipment(newValue);
        });
    }

    private void filterEquipment(String searchText) {
        if (allEquipment == null) return;

        filteredEquipment.clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredEquipment.addAll(allEquipment);
        } else {
            String lowerCaseFilter = searchText.toLowerCase().trim();
            for (Equipment equipment : allEquipment) {
                if (equipment.getName().toLowerCase().contains(lowerCaseFilter) ||
                    (equipment.getDescription() != null && equipment.getDescription().toLowerCase().contains(lowerCaseFilter))) {
                    filteredEquipment.add(equipment);
                }
            }
        }
    }

    private void loadEquipment() {
        setLoadingVisible(true);

        Task<List<Equipment>> task = new Task<List<Equipment>>() {
            @Override
            protected List<Equipment> call() throws Exception {
                return serviceManager.getEquipmentService().getAllEquipment();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    List<Equipment> equipment = task.getValue();
                    allEquipment.clear();
                    allEquipment.addAll(equipment);
                    filterEquipment(searchField.getText());
                    updateStats();
                    setLoadingVisible(false);
                    System.out.println("âœ… " + equipment.size() + " ekipman yÃ¼klendi");
                } catch (Exception ex) {
                    System.err.println("âŒ Ekipman yÃ¼kleme hatasÄ±: " + ex.getMessage());
                    showAlert("Hata", "Ekipmanlar yÃ¼klenirken hata oluÅŸtu");
                    setLoadingVisible(false);
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("âŒ Ekipman yÃ¼kleme task hatasÄ±: " + task.getException().getMessage());
                showAlert("Hata", "Ekipmanlar yÃ¼klenirken hata oluÅŸtu: " + task.getException().getMessage());
                setLoadingVisible(false);
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStats() {
        try {
            if (totalEquipmentLabel != null) {
                totalEquipmentLabel.setText(String.valueOf(allEquipment != null ? allEquipment.size() : 0));
            }

            // Calculate total exercises using equipment
            int totalExerciseCount = 0;
            String mostPopularEquipment = "-";
            int maxExerciseCount = 0;

            if (allEquipment != null) {
                for (Equipment equipment : allEquipment) {
                    int exerciseCount = getExerciseCountForEquipment(equipment.getId());
                    totalExerciseCount += exerciseCount;
                    
                    if (exerciseCount > maxExerciseCount) {
                        maxExerciseCount = exerciseCount;
                        mostPopularEquipment = equipment.getName();
                    }
                }
            }

            if (totalExercisesLabel != null) {
                totalExercisesLabel.setText(String.valueOf(totalExerciseCount));
            }
            if (popularEquipmentLabel != null) {
                popularEquipmentLabel.setText(mostPopularEquipment);
            }

        } catch (Exception e) {
            System.err.println("âŒ Stats gÃ¼ncelleme hatasÄ±: " + e.getMessage());
        }
    }

    private int getExerciseCountForEquipment(int equipmentId) {
        try {
            // Get exercises that use this equipment
            return serviceManager.getExerciseService().getExercisesByEquipment(equipmentId).size();
        } catch (Exception e) {
            System.err.println("âŒ Exercise count alÄ±namadÄ±: " + e.getMessage());
            return 0;
        }
    }

    // Event Handlers

    @FXML
    private void handleAddEquipment() {
        showEquipmentDialog(null);
    }

    @FXML
    private void handleRefresh() {
        loadEquipment();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterEquipment("");
    }

    private void handleEditEquipment(Equipment equipment) {
        showEquipmentDialog(equipment);
    }

    private void handleDeleteEquipment(Equipment equipment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ekipman Sil");
        alert.setHeaderText("EkipmanÄ± silmek istediÄŸinizden emin misiniz?");
        alert.setContentText("Ekipman: " + equipment.getName() + "\n\nBu iÅŸlem geri alÄ±namaz!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteEquipment(equipment);
        }
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

        Optional<Equipment> result = dialog.showAndWait();
        result.ifPresent(eq -> {
            if (equipment == null) {
                saveEquipment(eq);
            } else {
                updateEquipment(eq);
            }
        });
    }

    private void saveEquipment(Equipment equipment) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getEquipmentService().createEquipment(equipment);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla eklendi!");
                loadEquipment();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Ekipman eklenirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateEquipment(Equipment equipment) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getEquipmentService().updateEquipment(equipment);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla gÃ¼ncellendi!");
                loadEquipment();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Ekipman gÃ¼ncellenirken hata oluÅŸtu: " + task.getException().getMessage());
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void deleteEquipment(Equipment equipment) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                serviceManager.getEquipmentService().deleteEquipment(equipment.getId());
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showAlert("BaÅŸarÄ±lÄ±", "Ekipman baÅŸarÄ±yla silindi!");
                loadEquipment();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Hata", "Ekipman silinirken hata oluÅŸtu: " + task.getException().getMessage());
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
        System.out.println("âœ… EquipmentManagement current user set: " + (user != null ? user.getUsername() : "null"));
    }
} 



