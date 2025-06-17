package com.example.selfworkout.dao;

import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MuscleGroups tablosu için Data Access Object sınıfı
 * Kas grubu verilerini yönetir
 */
public class MuscleGroupDAO {
    
    // SQL sorguları
    private static final String INSERT_MUSCLE_GROUP = 
        "INSERT INTO MuscleGroups (name, description) VALUES (?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM MuscleGroups WHERE id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM MuscleGroups ORDER BY name";
    
    private static final String SELECT_BY_NAME = 
        "SELECT * FROM MuscleGroups WHERE name = ?";
    
    private static final String UPDATE_MUSCLE_GROUP = 
        "UPDATE MuscleGroups SET name = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_MUSCLE_GROUP = 
        "DELETE FROM MuscleGroups WHERE id = ?";
    
    /**
     * Yeni kas grubu ekler
     */
    public MuscleGroup save(MuscleGroup muscleGroup) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MUSCLE_GROUP, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, muscleGroup.getName());
            statement.setString(2, muscleGroup.getDescription());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Kas grubu ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    muscleGroup.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Kas grubu başarıyla eklendi: " + muscleGroup.getName());
                    return muscleGroup;
                } else {
                    throw new SQLException("Kas grubu ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre kas grubu bulur
     */
    public MuscleGroup findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToMuscleGroup(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * İsme göre kas grubu bulur
     */
    public MuscleGroup findByName(String name) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {
            
            statement.setString(1, name);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToMuscleGroup(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Tüm kas gruplarını getirir
     */
    public List<MuscleGroup> findAll() throws SQLException {
        List<MuscleGroup> muscleGroups = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                muscleGroups.add(mapResultSetToMuscleGroup(resultSet));
            }
        }
        
        return muscleGroups;
    }
    
    /**
     * Kas grubunu günceller
     */
    public boolean update(MuscleGroup muscleGroup) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_MUSCLE_GROUP)) {
            
            statement.setString(1, muscleGroup.getName());
            statement.setString(2, muscleGroup.getDescription());
            statement.setInt(3, muscleGroup.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Kas grubu başarıyla güncellendi: " + muscleGroup.getName());
                return true;
            }
            return false;
        }
    }
    
    /**
     * Kas grubunu siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_MUSCLE_GROUP)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Kas grubu başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * ResultSet'ten MuscleGroup nesnesini oluşturur
     */
    private MuscleGroup mapResultSetToMuscleGroup(ResultSet resultSet) throws SQLException {
        MuscleGroup muscleGroup = new MuscleGroup();
        muscleGroup.setId(resultSet.getInt("id"));
        muscleGroup.setName(resultSet.getString("name"));
        muscleGroup.setDescription(resultSet.getString("description"));
        return muscleGroup;
    }
} 