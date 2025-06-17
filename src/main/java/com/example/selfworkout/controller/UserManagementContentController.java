package com.example.selfworkout.controller;

import com.example.selfworkout.model.User;
import com.example.selfworkout.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementContentController implements Initializable {

    // Stats Labels
    @FXML private Label totalUsersLabel;
    @FXML private Label adminUsersLabel;
    @FXML private Label sportUsersLabel;
    @FXML private Label recentUsersLabel;
    
    // Search and Filter Elements
    @FXML private TextField searchField;
    @FXML private Button refreshBtn;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Table
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    
    // Detay Paneli ElemanlarÄ±
    @FXML private Label detailTitleLabel;
    @FXML private Label selectedUserLabel;
    @FXML private Label profileIconLabel;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label createdAtLabel;
    @FXML private VBox createdAtSection;
    
    // CRUD ButonlarÄ±
    @FXML private Button saveBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private Button addUserBtn;
    
    // Services
    private ServiceManager serviceManager;
    private UserService userService;
    private User currentUser;
    private User selectedUser;
    private List<User> allUsers;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ğŸ”„ UserManagementContent initialize baÅŸlatÄ±lÄ±yor...");
        
        try {
            initializeServices();
            setupTable();
            setupForm();
            loadUsers();
            
            System.out.println("âœ… UserManagementContent initialize tamamlandÄ±");
        } catch (Exception e) {
            System.err.println("âŒ UserManagementContent initialize hatasÄ±: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeServices() {
        try {
            serviceManager = ServiceManager.getInstance();
            userService = serviceManager.getUserService();
            currentUser = serviceManager.getAuthenticationService().getCurrentUser();
        } catch (Exception e) {
            System.err.println("âŒ UserManagementContent Service initialization hatasÄ±: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        // ID column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Username column
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        // Full name column
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        
        // Email column
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Role column
        roleColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String roleText = user.getRole() != null ? user.getRole().getRoleName() : "sporcu";
            return new SimpleStringProperty(roleText);
        });
        
        // Status column
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Aktif"));
        
        // Table selection listener
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectUser(newSelection);
            }
        });
    }
    
    private void setupForm() {
        // Role ComboBox setup
        roleComboBox.getItems().addAll("sporcu", "admin");
        roleComboBox.setValue("sporcu");
        
        // Initially hide created date section for new users
        createdAtSection.setVisible(false);
        
        // Clear form initially
        clearForm();
    }
    
    private void selectUser(User user) {
        this.selectedUser = user;
        
        // Update form fields
        selectedUserLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        passwordField.setText(""); // Don't show password
        roleComboBox.setValue(user.getRole() != null ? user.getRole().getRoleName() : "sporcu");
        
        // Show created date
        if (user.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            createdAtLabel.setText(user.getCreatedAt().format(formatter));
            createdAtSection.setVisible(true);
        }
        
        // Update profile icon based on role
        if (user.getRole() != null && "admin".equals(user.getRole().getRoleName())) {
            profileIconLabel.setText("ğŸ‘‘");
        } else {
            profileIconLabel.setText("ğŸ‘¤");
        }
        
        // Update detail title
        detailTitleLabel.setText("âœï¸ " + user.getUsername() + " - KullanÄ±cÄ± DÃ¼zenleme");
    }
    
    private void clearForm() {
        selectedUser = null;
        selectedUserLabel.setText("KullanÄ±cÄ± seÃ§ilmedi");
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue("sporcu");
        createdAtSection.setVisible(false);
        profileIconLabel.setText("ğŸ‘¤");
        detailTitleLabel.setText("ğŸ‘¤ KullanÄ±cÄ± DetaylarÄ±");
    }
    
    private void loadUsers() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        
        Task<List<User>> task = new Task<List<User>>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllUsers();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    allUsers = getValue();
                    usersTable.getItems().setAll(allUsers);
                    updateStats();
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Hata", "KullanÄ±cÄ±lar yÃ¼klenirken hata oluÅŸtu: " + getException().getMessage());
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void updateStats() {
        if (allUsers != null) {
            int total = allUsers.size();
            long adminCount = allUsers.stream().filter(u -> u.getRole() != null && "admin".equals(u.getRole().getRoleName())).count();
            long userCount = total - adminCount;
            
            // Son 30 gÃ¼nde kayÄ±t olan kullanÄ±cÄ±lar
            java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
            long recentCount = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
            
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
            if (adminUsersLabel != null) adminUsersLabel.setText(String.valueOf(adminCount));
            if (sportUsersLabel != null) sportUsersLabel.setText(String.valueOf(userCount));
            if (recentUsersLabel != null) recentUsersLabel.setText(String.valueOf(recentCount));
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadUsers();
    }
    
    @FXML
    private void handleAddUser() {
        clearForm();
        detailTitleLabel.setText("â• Yeni KullanÄ±cÄ± Ekleme");
    }
    
    @FXML
    private void handleSave() {
        try {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String role = roleComboBox.getValue();
            
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert("UyarÄ±", "LÃ¼tfen tÃ¼m zorunlu alanlarÄ± doldurun!");
                return;
            }
            
            User newUser = new User(username, email, password);
            
            // Set name and surname from fullName
            if (!fullName.isEmpty()) {
                String[] parts = fullName.split("\\s+", 2);
                newUser.setName(parts[0]);
                if (parts.length > 1) {
                    newUser.setSurname(parts[1]);
                }
            }
            
            // Set role based on selection
            if ("admin".equals(role)) {
                newUser.setRoleId(1);
            } else {
                newUser.setRoleId(2);
            }
            
            // UserService Ã¼zerinden kaydet
            userService.createUser(newUser);
            showAlert("BaÅŸarÄ±lÄ±", "KullanÄ±cÄ± baÅŸarÄ±yla oluÅŸturuldu!");
            clearForm();
            loadUsers();
            
        } catch (Exception e) {
            showAlert("Hata", "KullanÄ±cÄ± oluÅŸturulurken hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            showAlert("UyarÄ±", "LÃ¼tfen gÃ¼ncellenecek kullanÄ±cÄ±yÄ± seÃ§in!");
            return;
        }
        
                 try {
            selectedUser.setUsername(usernameField.getText().trim());
            
            // Set name and surname from fullName
            String fullName = fullNameField.getText().trim();
            if (!fullName.isEmpty()) {
                String[] parts = fullName.split("\\s+", 2);
                selectedUser.setName(parts[0]);
                if (parts.length > 1) {
                    selectedUser.setSurname(parts[1]);
                } else {
                    selectedUser.setSurname("");
                }
            }
            
            selectedUser.setEmail(emailField.getText().trim());
            
            if (!passwordField.getText().isEmpty()) {
                selectedUser.setPassword(passwordField.getText());
            }
            
            // Update role
            String role = roleComboBox.getValue();
            if ("admin".equals(role)) {
                selectedUser.setRoleId(1);
            } else {
                selectedUser.setRoleId(2);
            }
            
            userService.updateUser(selectedUser);
            showAlert("BaÅŸarÄ±lÄ±", "KullanÄ±cÄ± baÅŸarÄ±yla gÃ¼ncellendi!");
            loadUsers();
            
        } catch (Exception e) {
            showAlert("Hata", "KullanÄ±cÄ± gÃ¼ncellenirken hata oluÅŸtu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showAlert("UyarÄ±", "LÃ¼tfen silinecek kullanÄ±cÄ±yÄ± seÃ§in!");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("KullanÄ±cÄ± Silme OnayÄ±");
        confirmAlert.setHeaderText("KullanÄ±cÄ±yÄ± silmek istediÄŸinizden emin misiniz?");
        confirmAlert.setContentText("Bu iÅŸlem geri alÄ±namaz!\n\nKullanÄ±cÄ±: " + selectedUser.getUsername());
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.deleteUser(selectedUser.getId());
                showAlert("BaÅŸarÄ±lÄ±", "KullanÄ±cÄ± baÅŸarÄ±yla silindi!");
                clearForm();
                loadUsers();
                
            } catch (Exception e) {
                showAlert("Hata", "KullanÄ±cÄ± silinirken hata oluÅŸtu: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClear() {
        clearForm();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("âœ… UserManagementContent: Current user set to " + (user != null ? user.getUsername() : "null"));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 



