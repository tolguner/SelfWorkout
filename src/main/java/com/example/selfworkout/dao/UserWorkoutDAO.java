package com.example.selfworkout.dao;

import com.example.selfworkout.model.UserWorkout;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserWorkouts tablosu için Data Access Object sınıfı
 * Kullanıcı antrenmanlarını yönetir
 */
public class UserWorkoutDAO {
    
    // SQL sorguları
    private static final String INSERT_USER_WORKOUT = 
        "INSERT INTO UserWorkouts (user_id, routine_id, workout_date, workout_type, notes, status, duration_minutes, started_at, completed_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM UserWorkouts WHERE id = ?";
    
    private static final String SELECT_BY_USER_ID = 
        "SELECT * FROM UserWorkouts WHERE user_id = ? ORDER BY workout_date DESC";
    
    private static final String SELECT_BY_USER_AND_DATE = 
        "SELECT * FROM UserWorkouts WHERE user_id = ? AND workout_date = ?";
    
    private static final String UPDATE_USER_WORKOUT = 
        "UPDATE UserWorkouts SET routine_id = ?, workout_date = ?, workout_type = ?, notes = ?, status = ?, " +
        "duration_minutes = ?, started_at = ?, completed_at = ? WHERE id = ?";
    
    private static final String DELETE_USER_WORKOUT = 
        "DELETE FROM UserWorkouts WHERE id = ?";
    
    private static final String UPDATE_WORKOUT_STATUS = 
        "UPDATE UserWorkouts SET status = ?, completed_at = ? WHERE id = ?";
    
    private static final String SELECT_ACTIVE_WORKOUTS = 
        "SELECT * FROM UserWorkouts WHERE user_id = ? AND status = 'active' ORDER BY started_at DESC";
    
    private static final String SELECT_BY_USER_AND_EXERCISE = 
        "SELECT * FROM UserWorkouts WHERE user_id = ? AND exercise_id = ?";
    
    /**
     * Yeni kullanıcı antrenmanı ekler
     */
    public UserWorkout save(UserWorkout userWorkout) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER_WORKOUT, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, userWorkout.getUserId());
            
            if (userWorkout.getRoutineId() != null) {
                statement.setInt(2, userWorkout.getRoutineId());
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            
            statement.setDate(3, Date.valueOf(userWorkout.getWorkoutDate()));
            statement.setString(4, userWorkout.getWorkoutType());
            statement.setString(5, userWorkout.getNotes());
            statement.setString(6, userWorkout.getStatus());
            
            if (userWorkout.getDurationMinutes() != null) {
                statement.setInt(7, userWorkout.getDurationMinutes());
            } else {
                statement.setNull(7, Types.INTEGER);
            }
            
            if (userWorkout.getStartedAt() != null) {
                statement.setTimestamp(8, Timestamp.valueOf(userWorkout.getStartedAt()));
            } else {
                statement.setNull(8, Types.TIMESTAMP);
            }
            
            if (userWorkout.getCompletedAt() != null) {
                statement.setTimestamp(9, Timestamp.valueOf(userWorkout.getCompletedAt()));
            } else {
                statement.setNull(9, Types.TIMESTAMP);
            }
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Antrenman ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userWorkout.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Antrenman başarıyla eklendi.");
                    return userWorkout;
                } else {
                    throw new SQLException("Antrenman ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre antrenman bulur
     */
    public UserWorkout findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUserWorkout(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Kullanıcının tüm antrenmanlarını getirir
     */
    public List<UserWorkout> findByUserId(int userId) throws SQLException {
        return findByUserId(userId, 0); // Limit yok
    }
    
    /**
     * Kullanıcının antrenmanlarını limit ile getirir
     */
    public List<UserWorkout> findByUserId(int userId, int limit) throws SQLException {
        String query = SELECT_BY_USER_ID;
        if (limit > 0) {
            query += " ORDER BY started_at DESC OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        } else {
            query += " ORDER BY started_at DESC";
        }
        
        List<UserWorkout> userWorkouts = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    userWorkouts.add(mapResultSetToUserWorkout(resultSet));
                }
            }
        }
        
        return userWorkouts;
    }
    
    /**
     * Kullanıcının aktif antrenmanlarını getirir
     */
    public List<UserWorkout> findActiveWorkouts(int userId) throws SQLException {
        List<UserWorkout> activeWorkouts = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVE_WORKOUTS)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activeWorkouts.add(mapResultSetToUserWorkout(resultSet));
                }
            }
        }
        
        return activeWorkouts;
    }
    
    /**
     * Kullanıcının belirli egzersiz için antrenmanlarını getirir
     */
    public List<UserWorkout> findByUserAndExercise(int userId, int exerciseId, int limit) throws SQLException {
        String query = SELECT_BY_USER_AND_EXERCISE;
        if (limit > 0) {
            query += " ORDER BY started_at DESC OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        } else {
            query += " ORDER BY started_at DESC";
        }
        
        List<UserWorkout> userWorkouts = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, userId);
            statement.setInt(2, exerciseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    userWorkouts.add(mapResultSetToUserWorkout(resultSet));
                }
            }
        }
        
        return userWorkouts;
    }
    
    /**
     * Tüm antrenmanları getirir
     */
    public List<UserWorkout> findAll() throws SQLException {
        List<UserWorkout> workouts = new ArrayList<>();
        String sql = "SELECT * FROM UserWorkouts ORDER BY workout_date DESC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                workouts.add(mapResultSetToUserWorkout(resultSet));
            }
        }
        
        return workouts;
    }
    
    /**
     * Kullanıcının belirli tarihteki antrenmanlarını getirir
     */
    public List<UserWorkout> findByUserAndDate(int userId, LocalDate date) throws SQLException {
        List<UserWorkout> userWorkouts = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_AND_DATE)) {
            
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(date));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    userWorkouts.add(mapResultSetToUserWorkout(resultSet));
                }
            }
        }
        
        return userWorkouts;
    }
    
    /**
     * Antrenmanı günceller
     */
    public boolean update(UserWorkout userWorkout) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER_WORKOUT)) {
            
            if (userWorkout.getRoutineId() != null) {
                statement.setInt(1, userWorkout.getRoutineId());
            } else {
                statement.setNull(1, Types.INTEGER);
            }
            
            statement.setDate(2, Date.valueOf(userWorkout.getWorkoutDate()));
            statement.setString(3, userWorkout.getWorkoutType());
            statement.setString(4, userWorkout.getNotes());
            statement.setString(5, userWorkout.getStatus());
            
            if (userWorkout.getDurationMinutes() != null) {
                statement.setInt(6, userWorkout.getDurationMinutes());
            } else {
                statement.setNull(6, Types.INTEGER);
            }
            
            if (userWorkout.getStartedAt() != null) {
                statement.setTimestamp(7, Timestamp.valueOf(userWorkout.getStartedAt()));
            } else {
                statement.setNull(7, Types.TIMESTAMP);
            }
            
            if (userWorkout.getCompletedAt() != null) {
                statement.setTimestamp(8, Timestamp.valueOf(userWorkout.getCompletedAt()));
            } else {
                statement.setNull(8, Types.TIMESTAMP);
            }
            
            statement.setInt(9, userWorkout.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Antrenman başarıyla güncellendi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Antrenman durumunu günceller
     */
    public boolean updateStatus(int workoutId, String status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_WORKOUT_STATUS)) {
            
            statement.setString(1, status);
            
            if ("tamamlandı".equals(status)) {
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                statement.setNull(2, Types.TIMESTAMP);
            }
            
            statement.setInt(3, workoutId);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Antrenman durumu güncellendi: " + status);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Antrenmanı siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER_WORKOUT)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Antrenman başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * ResultSet'ten UserWorkout nesnesini oluşturur
     */
    private UserWorkout mapResultSetToUserWorkout(ResultSet resultSet) throws SQLException {
        UserWorkout userWorkout = new UserWorkout();
        userWorkout.setId(resultSet.getInt("id"));
        userWorkout.setUserId(resultSet.getInt("user_id"));
        
        // Routine ID nullable
        int routineId = resultSet.getInt("routine_id");
        if (!resultSet.wasNull()) {
            userWorkout.setRoutineId(routineId);
        }
        
        userWorkout.setWorkoutDate(resultSet.getDate("workout_date").toLocalDate());
        userWorkout.setWorkoutType(resultSet.getString("workout_type"));
        userWorkout.setNotes(resultSet.getString("notes"));
        userWorkout.setStatus(resultSet.getString("status"));
        
        // Duration nullable
        int duration = resultSet.getInt("duration_minutes");
        if (!resultSet.wasNull()) {
            userWorkout.setDurationMinutes(duration);
        }
        
        // Timestamps nullable
        Timestamp startedAt = resultSet.getTimestamp("started_at");
        if (startedAt != null) {
            userWorkout.setStartedAt(startedAt.toLocalDateTime());
        }
        
        Timestamp completedAt = resultSet.getTimestamp("completed_at");
        if (completedAt != null) {
            userWorkout.setCompletedAt(completedAt.toLocalDateTime());
        }
        
        return userWorkout;
    }
} 