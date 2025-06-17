package com.example.selfworkout.dao;

import com.example.selfworkout.model.Role;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Role tablosu için Data Access Object sınıfı
 * CRUD operasyonlarını yönetir
 */
public class RoleDAO {
    
    // SQL sorguları
    private static final String INSERT_ROLE = 
        "INSERT INTO Roles (role_name, description) VALUES (?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM Roles WHERE id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM Roles ORDER BY role_name";
    
    private static final String SELECT_BY_NAME = 
        "SELECT * FROM Roles WHERE role_name = ?";
    
    private static final String UPDATE_ROLE = 
        "UPDATE Roles SET role_name = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_ROLE = 
        "DELETE FROM Roles WHERE id = ?";
    
    private static final String COUNT_USERS_BY_ROLE = 
        "SELECT COUNT(*) FROM Users WHERE role_id = ?";
    
    /**
     * Yeni rol ekler
     */
    public Role save(Role role) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ROLE, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, role.getRoleName());
            statement.setString(2, role.getDescription());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Rol ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            // Oluşturulan ID'yi al
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    role.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Rol başarıyla eklendi: " + role.getRoleName());
                    return role;
                } else {
                    throw new SQLException("Rol ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre rol bulur
     */
    public Role findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToRole(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Rol adına göre bulur
     */
    public Role findByName(String roleName) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {
            
            statement.setString(1, roleName);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToRole(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Tüm rolleri getirir
     */
    public List<Role> findAll() throws SQLException {
        List<Role> roles = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                roles.add(mapResultSetToRole(resultSet));
            }
        }
        
        return roles;
    }
    
    /**
     * Rolü günceller
     */
    public boolean update(Role role) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE)) {
            
            statement.setString(1, role.getRoleName());
            statement.setString(2, role.getDescription());
            statement.setInt(3, role.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Rol başarıyla güncellendi: " + role.getRoleName());
                return true;
            }
            return false;
        }
    }
    
    /**
     * Rolü siler (eğer kullanıcı yoksa)
     */
    public boolean delete(int id) throws SQLException {
        // Önce bu role sahip kullanıcı var mı kontrol et
        if (hasUsers(id)) {
            throw new SQLException("Bu role sahip kullanıcılar bulunduğu için silinemez.");
        }
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ROLE)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Rol başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Bu role sahip kullanıcı var mı kontrol eder
     */
    public boolean hasUsers(int roleId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_USERS_BY_ROLE)) {
            
            statement.setInt(1, roleId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * ResultSet'ten Role nesnesini oluşturur
     */
    private Role mapResultSetToRole(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getInt("id"));
        role.setRoleName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("description"));
        return role;
    }
} 