package com.example.selfworkout.dao;

import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercise tablosu için Data Access Object sınıfı
 * Egzersiz verilerini ve ilişkili kas grupları/ekipmanları yönetir
 */
public class ExerciseDAO {
    
    // SQL sorguları - Ana Exercise tablosu
    private static final String INSERT_EXERCISE = 
        "INSERT INTO Exercises (name, description, difficulty_level, instructions, created_by) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM Exercises WHERE id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM Exercises ORDER BY name";
    
    private static final String SELECT_BY_NAME = 
        "SELECT * FROM Exercises WHERE name LIKE ?";
    
    private static final String SELECT_BY_DIFFICULTY = 
        "SELECT * FROM Exercises WHERE difficulty_level = ? ORDER BY name";
    
    private static final String UPDATE_EXERCISE = 
        "UPDATE Exercises SET name = ?, description = ?, difficulty_level = ?, instructions = ? WHERE id = ?";
    
    private static final String DELETE_EXERCISE = 
        "DELETE FROM Exercises WHERE id = ?";
    
    // Many-to-many ilişkiler için SQL sorguları
    private static final String SELECT_MUSCLE_GROUPS_BY_EXERCISE = 
        "SELECT mg.* FROM MuscleGroups mg " +
        "INNER JOIN ExerciseMuscles em ON mg.id = em.muscle_id " +
        "WHERE em.exercise_id = ?";
    
    private static final String SELECT_EQUIPMENTS_BY_EXERCISE = 
        "SELECT eq.* FROM Equipments eq " +
        "INNER JOIN ExerciseEquipments ee ON eq.id = ee.equipment_id " +
        "WHERE ee.exercise_id = ?";
    
    private static final String INSERT_EXERCISE_MUSCLE = 
        "INSERT INTO ExerciseMuscles (exercise_id, muscle_id) VALUES (?, ?)";
    
    private static final String INSERT_EXERCISE_EQUIPMENT = 
        "INSERT INTO ExerciseEquipments (exercise_id, equipment_id) VALUES (?, ?)";
    
    private static final String DELETE_EXERCISE_MUSCLES = 
        "DELETE FROM ExerciseMuscles WHERE exercise_id = ?";
    
    private static final String DELETE_EXERCISE_EQUIPMENTS = 
        "DELETE FROM ExerciseEquipments WHERE exercise_id = ?";
    
    /**
     * Yeni egzersiz ekler (kas grupları ve ekipmanlar ile birlikte)
     */
    public Exercise save(Exercise exercise) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // Transaction başlat
            
            // 1. Ana egzersizi ekle
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, exercise.getName());
                statement.setString(2, exercise.getDescription());
                statement.setString(3, exercise.getDifficultyLevel());
                statement.setString(4, exercise.getInstructions());
                statement.setInt(5, exercise.getCreatedBy());
                
                int affectedRows = statement.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Egzersiz ekleme başarısız, hiçbir satır etkilenmedi.");
                }
                
                // ID'yi al
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exercise.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Egzersiz ekleme başarısız, ID alınamadı.");
                    }
                }
            }
            
            // 2. Kas grupları ilişkilerini ekle
            if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_MUSCLE)) {
                    for (MuscleGroup muscleGroup : exercise.getMuscleGroups()) {
                        statement.setLong(1, exercise.getId());
                        statement.setInt(2, muscleGroup.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
            
            // 3. Ekipman ilişkilerini ekle
            if (exercise.getEquipments() != null && !exercise.getEquipments().isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_EQUIPMENT)) {
                    for (Equipment equipment : exercise.getEquipments()) {
                        statement.setLong(1, exercise.getId());
                        statement.setInt(2, equipment.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
            
            connection.commit(); // Transaction başarılı
            System.out.println("✅ Egzersiz başarıyla eklendi: " + exercise.getName());
            return exercise;
            
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // Hata durumunda geri al
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
    
    /**
     * ID'ye göre egzersiz bulur (tüm ilişkiler ile birlikte)
     */
    public Exercise findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Exercise exercise = mapResultSetToExercise(resultSet);
                    
                    // Kas gruplarını yükle
                    exercise.setMuscleGroups(findMuscleGroupsByExercise(id));
                    
                    // Ekipmanları yükle
                    exercise.setEquipments(findEquipmentsByExercise(id));
                    
                    return exercise;
                }
                return null;
            }
        }
    }
    
    /**
     * Tüm egzersizleri getirir
     */
    public List<Exercise> findAll() throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Exercise exercise = mapResultSetToExercise(resultSet);
                
                // Kas gruplarını ve ekipmanları yükle
                exercise.setMuscleGroups(findMuscleGroupsByExercise(exercise.getId()));
                exercise.setEquipments(findEquipmentsByExercise(exercise.getId()));
                
                exercises.add(exercise);
            }
        }
        
        return exercises;
    }
    
    /**
     * İsme göre egzersiz arar
     */
    public List<Exercise> findByName(String name) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {
            
            statement.setString(1, "%" + name + "%"); // LIKE sorgusu için
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapResultSetToExercise(resultSet));
                }
            }
        }
        
        return exercises;
    }
    
    /**
     * Zorluk seviyesine göre egzersizleri getirir
     */
    public List<Exercise> findByDifficulty(String difficultyLevel) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_DIFFICULTY)) {
            
            statement.setString(1, difficultyLevel);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapResultSetToExercise(resultSet));
                }
            }
        }
        
        return exercises;
    }
    
    /**
     * Egzersizi günceller (ilişkiler ile birlikte)
     */
    public boolean update(Exercise exercise) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            
            // 1. Ana egzersizi güncelle
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_EXERCISE)) {
                statement.setString(1, exercise.getName());
                statement.setString(2, exercise.getDescription());
                statement.setString(3, exercise.getDifficultyLevel());
                statement.setString(4, exercise.getInstructions());
                statement.setLong(5, exercise.getId());
                
                int affectedRows = statement.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
            }
            
            // 2. Eski ilişkileri sil
            deleteExerciseRelations(connection, exercise.getId());
            
            // 3. Yeni kas grupları ilişkilerini ekle
            if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_MUSCLE)) {
                    for (MuscleGroup muscleGroup : exercise.getMuscleGroups()) {
                        statement.setLong(1, exercise.getId());
                        statement.setInt(2, muscleGroup.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
            
            // 4. Yeni ekipman ilişkilerini ekle
            if (exercise.getEquipments() != null && !exercise.getEquipments().isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_EQUIPMENT)) {
                    for (Equipment equipment : exercise.getEquipments()) {
                        statement.setLong(1, exercise.getId());
                        statement.setInt(2, equipment.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
            
            connection.commit();
            System.out.println("✅ Egzersiz başarıyla güncellendi: " + exercise.getName());
            return true;
            
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
    
    /**
     * Egzersizi siler (tüm ilişkiler ile birlikte)
     */
    public boolean delete(int id) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            
            // 1. İlişkileri sil
            deleteExerciseRelations(connection, id);
            
            // 2. Ana egzersizi sil
            try (PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE)) {
                statement.setInt(1, id);
                
                int affectedRows = statement.executeUpdate();
                
                if (affectedRows > 0) {
                    connection.commit();
                    System.out.println("✅ Egzersiz başarıyla silindi.");
                    return true;
                }
                return false;
            }
            
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
    
    /**
     * Egzersize ait kas gruplarını getirir
     */
    private List<MuscleGroup> findMuscleGroupsByExercise(int exerciseId) throws SQLException {
        List<MuscleGroup> muscleGroups = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_MUSCLE_GROUPS_BY_EXERCISE)) {
            
            statement.setLong(1, exerciseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MuscleGroup muscleGroup = new MuscleGroup();
                    muscleGroup.setId(resultSet.getInt("id"));
                    muscleGroup.setName(resultSet.getString("name"));
                    muscleGroup.setDescription(resultSet.getString("description"));
                    muscleGroups.add(muscleGroup);
                }
            }
        }
        
        return muscleGroups;
    }
    
    /**
     * Egzersize ait ekipmanları getirir
     */
    private List<Equipment> findEquipmentsByExercise(int exerciseId) throws SQLException {
        List<Equipment> equipments = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_EQUIPMENTS_BY_EXERCISE)) {
            
            statement.setLong(1, exerciseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Equipment equipment = new Equipment();
                    equipment.setId(resultSet.getInt("id"));
                    equipment.setName(resultSet.getString("name"));
                    equipment.setDescription(resultSet.getString("description"));
                    equipments.add(equipment);
                }
            }
        }
        
        return equipments;
    }
    
    /**
     * Egzersizin tüm ilişkilerini siler
     */
    private void deleteExerciseRelations(Connection connection, int exerciseId) throws SQLException {
        // Kas grubu ilişkilerini sil
        try (PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_MUSCLES)) {
            statement.setLong(1, exerciseId);
            statement.executeUpdate();
        }
        
        // Ekipman ilişkilerini sil
        try (PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_EQUIPMENTS)) {
            statement.setLong(1, exerciseId);
            statement.executeUpdate();
        }
    }
    
    /**
     * ResultSet'ten Exercise nesnesini oluşturur
     */
    /**
     * Kas grubuna göre egzersizleri getirir
     */
    public List<Exercise> findByMuscleGroup(int muscleGroupId) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        
        String sql = "SELECT DISTINCT e.* FROM Exercises e " +
                     "INNER JOIN ExerciseMuscles em ON e.id = em.exercise_id " +
                     "WHERE em.muscle_id = ? ORDER BY e.name";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, muscleGroupId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Exercise exercise = mapResultSetToExercise(resultSet);
                    exercise.setMuscleGroups(findMuscleGroupsByExercise(exercise.getId()));
                    exercise.setEquipments(findEquipmentsByExercise(exercise.getId()));
                    exercises.add(exercise);
                }
            }
        }
        
        return exercises;
    }
    
    /**
     * Ekipmana göre egzersizleri getirir
     */
    public List<Exercise> findByEquipment(int equipmentId) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        
        String sql = "SELECT DISTINCT e.* FROM Exercises e " +
                     "INNER JOIN ExerciseEquipments ee ON e.id = ee.exercise_id " +
                     "WHERE ee.equipment_id = ? ORDER BY e.name";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, equipmentId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Exercise exercise = mapResultSetToExercise(resultSet);
                    exercise.setMuscleGroups(findMuscleGroupsByExercise(exercise.getId()));
                    exercise.setEquipments(findEquipmentsByExercise(exercise.getId()));
                    exercises.add(exercise);
                }
            }
        }
        
        return exercises;
    }

    private Exercise mapResultSetToExercise(ResultSet resultSet) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setId(resultSet.getInt("id"));
        exercise.setName(resultSet.getString("name"));
        exercise.setDescription(resultSet.getString("description"));
        exercise.setDifficultyLevel(resultSet.getString("difficulty_level"));
        exercise.setInstructions(resultSet.getString("instructions"));
        return exercise;
    }
} 