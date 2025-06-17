package com.example.selfworkout.dao;

import com.example.selfworkout.model.WorkoutExercise;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WorkoutExercises tablosu için Data Access Object sınıfı
 * Antrenman-egzersiz ilişkilerini yönetir
 */
public class WorkoutExerciseDAO {
    
    // SQL sorguları
    private static final String INSERT_WORKOUT_EXERCISE = 
        "INSERT INTO WorkoutExercises (workout_id, exercise_id, set_count, reps_per_set, weight_per_set) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT we.*, e.name as exercise_name " +
        "FROM WorkoutExercises we " +
        "LEFT JOIN Exercises e ON we.exercise_id = e.id " +
        "WHERE we.id = ?";
    
    private static final String SELECT_BY_WORKOUT_ID = 
        "SELECT we.*, e.name as exercise_name " +
        "FROM WorkoutExercises we " +
        "LEFT JOIN Exercises e ON we.exercise_id = e.id " +
        "WHERE we.workout_id = ?";
    
    private static final String DELETE_WORKOUT_EXERCISE = 
        "DELETE FROM WorkoutExercises WHERE id = ?";
    
    private static final String DELETE_BY_WORKOUT_ID = 
        "DELETE FROM WorkoutExercises WHERE workout_id = ?";
    
    /**
     * Yeni antrenman-egzersiz ilişkisi ekler
     */
    public WorkoutExercise save(WorkoutExercise workoutExercise) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_WORKOUT_EXERCISE, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, workoutExercise.getDailyWorkoutId());
            statement.setInt(2, workoutExercise.getExerciseId());
            statement.setInt(3, workoutExercise.getSetCount());
            statement.setString(4, workoutExercise.getRepsPerSet());
            statement.setString(5, workoutExercise.getWeightPerSet());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Antrenman-egzersiz ilişkisi ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    workoutExercise.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Antrenman-egzersiz ilişkisi başarıyla eklendi.");
                    return workoutExercise;
                } else {
                    throw new SQLException("Antrenman-egzersiz ilişkisi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre antrenman-egzersiz ilişkisi bulur
     */
    public WorkoutExercise findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToWorkoutExercise(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Antrenman ID'sine göre egzersizleri getirir
     */
    public List<WorkoutExercise> findByWorkoutId(int workoutId) throws SQLException {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_WORKOUT_ID)) {
            
            statement.setInt(1, workoutId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workoutExercises.add(mapResultSetToWorkoutExercise(resultSet));
                }
            }
        }
        
        return workoutExercises;
    }
    
    /**
     * Antrenman-egzersiz ilişkisini siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_WORKOUT_EXERCISE)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Antrenman-egzersiz ilişkisi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Belirli antrenmanın tüm egzersizlerini siler
     */
    public boolean deleteByWorkoutId(int workoutId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_WORKOUT_ID)) {
            
            statement.setInt(1, workoutId);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Antrenmanın tüm egzersizleri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }
    
    /**
     * ResultSet'ten WorkoutExercise nesnesini oluşturur
     */
    private WorkoutExercise mapResultSetToWorkoutExercise(ResultSet resultSet) throws SQLException {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(resultSet.getInt("id"));
        workoutExercise.setDailyWorkoutId(resultSet.getInt("workout_id"));
        workoutExercise.setExerciseId(resultSet.getInt("exercise_id"));
        workoutExercise.setSetCount(resultSet.getInt("set_count"));
        workoutExercise.setRepsPerSet(resultSet.getString("reps_per_set"));
        workoutExercise.setWeightPerSet(resultSet.getString("weight_per_set"));
        
        // İlişkili egzersiz objesi
        try {
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(workoutExercise.getExerciseId());
                exercise.setName(exerciseName);
                workoutExercise.setExercise(exercise);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }
        
        return workoutExercise;
    }
} 