package com.example.selfworkout.dao;

import com.example.selfworkout.model.RoutineExercise;
import com.example.selfworkout.model.ExerciseRoutine;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RoutineExercises tablosu için Data Access Object sınıfı
 * Rutin-egzersiz ilişkilerini yönetir
 */
public class RoutineExerciseDAO {
    
    // SQL sorguları
    private static final String INSERT_ROUTINE_EXERCISE = 
        "INSERT INTO RoutineExercises (routine_id, exercise_id, exercise_order, set_count, reps_per_set, weight_per_set) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT re.*, er.name as routine_name, e.name as exercise_name " +
        "FROM RoutineExercises re " +
        "LEFT JOIN ExerciseRoutines er ON re.routine_id = er.id " +
        "LEFT JOIN Exercises e ON re.exercise_id = e.id " +
        "WHERE re.id = ?";
    
    private static final String SELECT_BY_ROUTINE_ID = 
        "SELECT re.*, er.name as routine_name, e.name as exercise_name " +
        "FROM RoutineExercises re " +
        "LEFT JOIN ExerciseRoutines er ON re.routine_id = er.id " +
        "LEFT JOIN Exercises e ON re.exercise_id = e.id " +
        "WHERE re.routine_id = ? ORDER BY re.exercise_order";
    
    private static final String DELETE_ROUTINE_EXERCISE = 
        "DELETE FROM RoutineExercises WHERE id = ?";
    
    private static final String DELETE_BY_ROUTINE_ID = 
        "DELETE FROM RoutineExercises WHERE routine_id = ?";
    
    /**
     * Yeni rutin-egzersiz ilişkisi ekler
     */
    public RoutineExercise save(RoutineExercise routineExercise) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ROUTINE_EXERCISE, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, routineExercise.getRoutineId());
            statement.setInt(2, routineExercise.getExerciseId());
            statement.setInt(3, routineExercise.getExerciseOrder());
            statement.setInt(4, routineExercise.getSetCount());
            statement.setString(5, routineExercise.getRepsPerSet());
            statement.setString(6, routineExercise.getWeightPerSet());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Rutin-egzersiz ilişkisi ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    routineExercise.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Rutin-egzersiz ilişkisi başarıyla eklendi.");
                    return routineExercise;
                } else {
                    throw new SQLException("Rutin-egzersiz ilişkisi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre rutin-egzersiz ilişkisi bulur
     */
    public RoutineExercise findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToRoutineExercise(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Rutin ID'sine göre egzersizleri getirir (sıralı)
     */
    public List<RoutineExercise> findByRoutineId(int routineId) throws SQLException {
        List<RoutineExercise> routineExercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ROUTINE_ID)) {
            
            statement.setInt(1, routineId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    routineExercises.add(mapResultSetToRoutineExercise(resultSet));
                }
            }
        }
        
        return routineExercises;
    }
    
    /**
     * Rutin-egzersiz ilişkisini siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ROUTINE_EXERCISE)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Rutin-egzersiz ilişkisi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Belirli rutinin tüm egzersizlerini siler
     */
    public boolean deleteByRoutineId(int routineId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ROUTINE_ID)) {
            
            statement.setInt(1, routineId);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Rutinin tüm egzersizleri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }
    
    /**
     * ResultSet'ten RoutineExercise nesnesini oluşturur
     */
    private RoutineExercise mapResultSetToRoutineExercise(ResultSet resultSet) throws SQLException {
        RoutineExercise routineExercise = new RoutineExercise();
        routineExercise.setId(resultSet.getInt("id"));
        routineExercise.setRoutineId(resultSet.getInt("routine_id"));
        routineExercise.setExerciseId(resultSet.getInt("exercise_id"));
        routineExercise.setExerciseOrder(resultSet.getInt("exercise_order"));
        routineExercise.setSetCount(resultSet.getInt("set_count"));
        routineExercise.setRepsPerSet(resultSet.getString("reps_per_set"));
        routineExercise.setWeightPerSet(resultSet.getString("weight_per_set"));
        
        // İlişkili objeler
        try {
            String routineName = resultSet.getString("routine_name");
            if (routineName != null) {
                ExerciseRoutine routine = new ExerciseRoutine();
                routine.setId(routineExercise.getRoutineId());
                routine.setName(routineName);
                routineExercise.setRoutine(routine);
            }
            
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(routineExercise.getExerciseId());
                exercise.setName(exerciseName);
                routineExercise.setExercise(exercise);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }
        
        return routineExercise;
    }
}
