package com.example.selfworkout.dao;

import com.example.selfworkout.model.ExerciseRoutine;
import com.example.selfworkout.model.User;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseRoutines tablosu için Data Access Object sınıfı
 * Egzersiz rutinlerini yönetir
 */
public class ExerciseRoutineDAO {
    
    // SQL sorguları
    private static final String INSERT_EXERCISE_ROUTINE = 
        "INSERT INTO ExerciseRoutines (user_id, name, description, created_at) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT er.*, u.username " +
        "FROM ExerciseRoutines er " +
        "LEFT JOIN Users u ON er.user_id = u.id " +
        "WHERE er.id = ?";
    
    private static final String SELECT_BY_USER_ID = 
        "SELECT er.*, u.username " +
        "FROM ExerciseRoutines er " +
        "LEFT JOIN Users u ON er.user_id = u.id " +
        "WHERE er.user_id = ? ORDER BY er.created_at DESC";
    
    private static final String SELECT_ALL = 
        "SELECT er.*, u.username " +
        "FROM ExerciseRoutines er " +
        "LEFT JOIN Users u ON er.user_id = u.id " +
        "ORDER BY er.created_at DESC";
    
    private static final String SELECT_BY_NAME = 
        "SELECT er.*, u.username " +
        "FROM ExerciseRoutines er " +
        "LEFT JOIN Users u ON er.user_id = u.id " +
        "WHERE er.name LIKE ? ORDER BY er.created_at DESC";
    
    private static final String UPDATE_EXERCISE_ROUTINE = 
        "UPDATE ExerciseRoutines SET name = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_EXERCISE_ROUTINE = 
        "DELETE FROM ExerciseRoutines WHERE id = ?";
    
    private static final String COUNT_BY_USER = 
        "SELECT COUNT(*) FROM ExerciseRoutines WHERE user_id = ?";
    
    private static final String CHECK_NAME_EXISTS = 
        "SELECT COUNT(*) FROM ExerciseRoutines WHERE user_id = ? AND name = ? AND id != ?";
    
    /**
     * Yeni egzersiz rutini ekler
     */
    public ExerciseRoutine save(ExerciseRoutine exerciseRoutine) throws SQLException {
        // İsim kontrolü
        if (isNameExists(exerciseRoutine.getUserId(), exerciseRoutine.getName(), 0)) {
            throw new SQLException("Bu isimde bir rutin zaten mevcut: " + exerciseRoutine.getName());
        }
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_ROUTINE, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, exerciseRoutine.getUserId());
            statement.setString(2, exerciseRoutine.getName());
            statement.setString(3, exerciseRoutine.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Egzersiz rutini ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exerciseRoutine.setId(generatedKeys.getInt(1));
                    exerciseRoutine.setCreatedAt(LocalDateTime.now());
                    System.out.println("✅ Egzersiz rutini başarıyla eklendi: " + exerciseRoutine.getName());
                    return exerciseRoutine;
                } else {
                    throw new SQLException("Egzersiz rutini ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre egzersiz rutini bulur
     */
    public ExerciseRoutine findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToExerciseRoutine(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Kullanıcı ID'sine göre egzersiz rutinlerini getirir
     */
    public List<ExerciseRoutine> findByUserId(int userId) throws SQLException {
        List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseRoutines.add(mapResultSetToExerciseRoutine(resultSet));
                }
            }
        }
        
        return exerciseRoutines;
    }
    
    /**
     * Kullanıcının sınırlı sayıda rutinini getirir
     */
    public List<ExerciseRoutine> findByUserId(int userId, int limit) throws SQLException {
        String sql = SELECT_BY_USER_ID;
        if (limit > 0) {
            sql += " OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        
        List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseRoutines.add(mapResultSetToExerciseRoutine(resultSet));
                }
            }
        }
        
        return exerciseRoutines;
    }
    
    /**
     * İsme göre egzersiz rutinlerini arar
     */
    public List<ExerciseRoutine> findByName(String name) throws SQLException {
        List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {
            
            statement.setString(1, "%" + name.trim() + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseRoutines.add(mapResultSetToExerciseRoutine(resultSet));
                }
            }
        }
        
        return exerciseRoutines;
    }
    
    /**
     * Tüm egzersiz rutinlerini getirir
     */
    public List<ExerciseRoutine> findAll() throws SQLException {
        List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                exerciseRoutines.add(mapResultSetToExerciseRoutine(resultSet));
            }
        }
        
        return exerciseRoutines;
    }
    
    /**
     * Egzersiz rutinini günceller
     */
    public boolean update(ExerciseRoutine exerciseRoutine) throws SQLException {
        // İsim kontrolü (kendi ID'si hariç)
        if (isNameExists(exerciseRoutine.getUserId(), exerciseRoutine.getName(), exerciseRoutine.getId())) {
            throw new SQLException("Bu isimde bir rutin zaten mevcut: " + exerciseRoutine.getName());
        }
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_EXERCISE_ROUTINE)) {
            
            statement.setString(1, exerciseRoutine.getName());
            statement.setString(2, exerciseRoutine.getDescription());
            statement.setInt(3, exerciseRoutine.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Egzersiz rutini başarıyla güncellendi: " + exerciseRoutine.getName());
                return true;
            }
            return false;
        }
    }
    
    /**
     * Egzersiz rutinini siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_ROUTINE)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Egzersiz rutini başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Kullanıcının rutin sayısını getirir
     */
    public int countByUserId(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_USER)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }
    
    /**
     * Kullanıcının belirli isimde rutininin var olup olmadığını kontrol eder
     */
    public boolean isNameExists(int userId, String name, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_NAME_EXISTS)) {
            
            statement.setInt(1, userId);
            statement.setString(2, name.trim());
            statement.setInt(3, excludeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * Kullanıcının popüler rutinlerini getirir (en çok kullanılan)
     */
    public List<ExerciseRoutine> findPopularByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT er.*, u.username, COUNT(uw.id) as usage_count " +
                    "FROM ExerciseRoutines er " +
                    "LEFT JOIN Users u ON er.user_id = u.id " +
                    "LEFT JOIN UserWorkouts uw ON er.id = uw.routine_id " +
                    "WHERE er.user_id = ? " +
                    "GROUP BY er.id, er.user_id, er.name, er.description, er.created_at, u.username " +
                    "ORDER BY usage_count DESC, er.created_at DESC";
        
        if (limit > 0) {
            sql += " OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        
        List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseRoutines.add(mapResultSetToExerciseRoutine(resultSet));
                }
            }
        }
        
        return exerciseRoutines;
    }
    
    /**
     * ResultSet'ten ExerciseRoutine nesnesini oluşturur
     */
    private ExerciseRoutine mapResultSetToExerciseRoutine(ResultSet resultSet) throws SQLException {
        ExerciseRoutine exerciseRoutine = new ExerciseRoutine();
        exerciseRoutine.setId(resultSet.getInt("id"));
        exerciseRoutine.setUserId(resultSet.getInt("user_id"));
        exerciseRoutine.setName(resultSet.getString("name"));
        exerciseRoutine.setDescription(resultSet.getString("description"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            exerciseRoutine.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // İlişkili kullanıcı objesi
        try {
            String username = resultSet.getString("username");
            if (username != null) {
                User user = new User();
                user.setId(exerciseRoutine.getUserId());
                user.setUsername(username);
                exerciseRoutine.setUser(user);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }
        
        return exerciseRoutine;
    }
} 