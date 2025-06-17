package com.example.selfworkout.dao;

import com.example.selfworkout.model.FavoriteExercise;
import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteExercises tablosu için Data Access Object sınıfı
 * Favori egzersizleri yönetir
 */
public class FavoriteExerciseDAO {
    
    // SQL sorguları
    private static final String INSERT_FAVORITE_EXERCISE = 
        "INSERT INTO FavoriteExercises (user_id, exercise_id, created_at) VALUES (?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT fe.*, u.username, e.name as exercise_name " +
        "FROM FavoriteExercises fe " +
        "LEFT JOIN Users u ON fe.user_id = u.id " +
        "LEFT JOIN Exercises e ON fe.exercise_id = e.id " +
        "WHERE fe.id = ?";
    
    private static final String SELECT_BY_USER_ID = 
        "SELECT fe.*, u.username, e.name as exercise_name " +
        "FROM FavoriteExercises fe " +
        "LEFT JOIN Users u ON fe.user_id = u.id " +
        "LEFT JOIN Exercises e ON fe.exercise_id = e.id " +
        "WHERE fe.user_id = ? ORDER BY fe.created_at DESC";
    
    private static final String DELETE_FAVORITE_EXERCISE = 
        "DELETE FROM FavoriteExercises WHERE id = ?";
    
    private static final String DELETE_BY_USER_AND_EXERCISE = 
        "DELETE FROM FavoriteExercises WHERE user_id = ? AND exercise_id = ?";
    
    private static final String CHECK_EXISTS = 
        "SELECT COUNT(*) FROM FavoriteExercises WHERE user_id = ? AND exercise_id = ?";
    
    /**
     * Yeni favori egzersiz ekler
     */
    public FavoriteExercise save(FavoriteExercise favoriteExercise) throws SQLException {
        // Önce aynı favori var mı kontrol et
        if (exists(favoriteExercise.getUserId(), favoriteExercise.getExerciseId())) {
            System.out.println("⚠️ Bu egzersiz zaten favorilerde!");
            return null;
        }
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_FAVORITE_EXERCISE, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, favoriteExercise.getUserId());
            statement.setInt(2, favoriteExercise.getExerciseId());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Favori egzersiz ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    favoriteExercise.setId(generatedKeys.getInt(1));
                    favoriteExercise.setCreatedAt(LocalDateTime.now());
                    System.out.println("✅ Favori egzersiz başarıyla eklendi.");
                    return favoriteExercise;
                } else {
                    throw new SQLException("Favori egzersiz ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre favori egzersiz bulur
     */
    public FavoriteExercise findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFavoriteExercise(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Kullanıcı ID'sine göre favori egzersizleri getirir
     */
    public List<FavoriteExercise> findByUserId(int userId) throws SQLException {
        List<FavoriteExercise> favoriteExercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    favoriteExercises.add(mapResultSetToFavoriteExercise(resultSet));
                }
            }
        }
        
        return favoriteExercises;
    }
    
    /**
     * Favori egzersizi siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_FAVORITE_EXERCISE)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Favori egzersiz başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Kullanıcı ve egzersiz ID'sine göre favori egzersizi siler
     */
    public boolean deleteByUserAndExercise(int userId, int exerciseId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_USER_AND_EXERCISE)) {
            
            statement.setInt(1, userId);
            statement.setInt(2, exerciseId);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Favori egzersiz başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Belirli kullanıcı-egzersiz kombinasyonunun var olup olmadığını kontrol eder
     */
    public boolean exists(int userId, int exerciseId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_EXISTS)) {
            
            statement.setInt(1, userId);
            statement.setInt(2, exerciseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * ResultSet'ten FavoriteExercise nesnesini oluşturur
     */
    private FavoriteExercise mapResultSetToFavoriteExercise(ResultSet resultSet) throws SQLException {
        FavoriteExercise favoriteExercise = new FavoriteExercise();
        favoriteExercise.setId(resultSet.getInt("id"));
        favoriteExercise.setUserId(resultSet.getInt("user_id"));
        favoriteExercise.setExerciseId(resultSet.getInt("exercise_id"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            favoriteExercise.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // İlişkili objeler
        try {
            String username = resultSet.getString("username");
            if (username != null) {
                User user = new User();
                user.setId(favoriteExercise.getUserId());
                user.setUsername(username);
                favoriteExercise.setUser(user);
            }
            
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(favoriteExercise.getExerciseId());
                exercise.setName(exerciseName);
                favoriteExercise.setExercise(exercise);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }
        
        return favoriteExercise;
    }
} 