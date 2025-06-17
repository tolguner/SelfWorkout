package com.example.selfworkout.dao;

import com.example.selfworkout.model.ExerciseMuscle;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseMuscles tablosu için Data Access Object sınıfı
 * Egzersiz-kas grubu ilişkilerini yönetir
 */
public class ExerciseMuscleDAO {

    // SQL sorguları
    private static final String INSERT_EXERCISE_MUSCLE =
            "INSERT INTO ExerciseMuscles (exercise_id, muscle_id) VALUES (?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT em.*, e.name as exercise_name, mg.name as muscle_name " +
                    "FROM ExerciseMuscles em " +
                    "LEFT JOIN Exercises e ON em.exercise_id = e.id " +
                    "LEFT JOIN MuscleGroups mg ON em.muscle_id = mg.id " +
                    "WHERE em.id = ?";

    private static final String SELECT_BY_EXERCISE_ID =
            "SELECT em.*, e.name as exercise_name, mg.name as muscle_name " +
                    "FROM ExerciseMuscles em " +
                    "LEFT JOIN Exercises e ON em.exercise_id = e.id " +
                    "LEFT JOIN MuscleGroups mg ON em.muscle_id = mg.id " +
                    "WHERE em.exercise_id = ?";

    private static final String SELECT_BY_MUSCLE_ID =
            "SELECT em.*, e.name as exercise_name, mg.name as muscle_name " +
                    "FROM ExerciseMuscles em " +
                    "LEFT JOIN Exercises e ON em.exercise_id = e.id " +
                    "LEFT JOIN MuscleGroups mg ON em.muscle_id = mg.id " +
                    "WHERE em.muscle_id = ?";

    private static final String SELECT_ALL =
            "SELECT em.*, e.name as exercise_name, mg.name as muscle_name " +
                    "FROM ExerciseMuscles em " +
                    "LEFT JOIN Exercises e ON em.exercise_id = e.id " +
                    "LEFT JOIN MuscleGroups mg ON em.muscle_id = mg.id " +
                    "ORDER BY e.name, mg.name";

    private static final String DELETE_EXERCISE_MUSCLE =
            "DELETE FROM ExerciseMuscles WHERE id = ?";

    private static final String DELETE_BY_EXERCISE_ID =
            "DELETE FROM ExerciseMuscles WHERE exercise_id = ?";

    private static final String DELETE_BY_MUSCLE_ID =
            "DELETE FROM ExerciseMuscles WHERE muscle_id = ?";

    private static final String CHECK_EXISTS =
            "SELECT COUNT(*) FROM ExerciseMuscles WHERE exercise_id = ? AND muscle_id = ?";

    /**
     * Yeni egzersiz-kas ilişkisi ekler
     */
    public ExerciseMuscle save(ExerciseMuscle exerciseMuscle) throws SQLException {
        // Önce aynı ilişki var mı kontrol et
        if (exists(exerciseMuscle.getExerciseId(), exerciseMuscle.getMuscleId())) {
            System.out.println("⚠️ Bu egzersiz-kas ilişkisi zaten mevcut!");
            return null;
        }

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_MUSCLE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exerciseMuscle.getExerciseId());
            statement.setInt(2, exerciseMuscle.getMuscleId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Egzersiz-kas ilişkisi ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exerciseMuscle.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Egzersiz-kas ilişkisi başarıyla eklendi.");
                    return exerciseMuscle;
                } else {
                    throw new SQLException("Egzersiz-kas ilişkisi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre egzersiz-kas ilişkisi bulur
     */
    public ExerciseMuscle findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToExerciseMuscle(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Egzersiz ID'sine göre kas gruplarını getirir
     */
    public List<ExerciseMuscle> findByExerciseId(int exerciseId) throws SQLException {
        List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseMuscles.add(mapResultSetToExerciseMuscle(resultSet));
                }
            }
        }

        return exerciseMuscles;
    }

    /**
     * Kas grubu ID'sine göre egzersizleri getirir
     */
    public List<ExerciseMuscle> findByMuscleId(int muscleId) throws SQLException {
        List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_MUSCLE_ID)) {

            statement.setInt(1, muscleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseMuscles.add(mapResultSetToExerciseMuscle(resultSet));
                }
            }
        }

        return exerciseMuscles;
    }

    /**
     * Tüm egzersiz-kas ilişkilerini getirir
     */
    public List<ExerciseMuscle> findAll() throws SQLException {
        List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                exerciseMuscles.add(mapResultSetToExerciseMuscle(resultSet));
            }
        }

        return exerciseMuscles;
    }

    /**
     * Egzersiz-kas ilişkisini siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_MUSCLE)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersiz-kas ilişkisi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersizin tüm kas ilişkilerini siler
     */
    public boolean deleteByExerciseId(int exerciseId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersizin tüm kas ilişkileri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli kas grubunun tüm egzersiz ilişkilerini siler
     */
    public boolean deleteByMuscleId(int muscleId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_MUSCLE_ID)) {

            statement.setInt(1, muscleId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Kas grubunun tüm egzersiz ilişkileri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersiz-kas ilişkisinin var olup olmadığını kontrol eder
     */
    public boolean exists(int exerciseId, int muscleId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_EXISTS)) {

            statement.setInt(1, exerciseId);
            statement.setInt(2, muscleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Toplu egzersiz-kas ilişkisi ekleme
     */
    public boolean saveAll(int exerciseId, List<Integer> muscleIds) throws SQLException {
        if (muscleIds == null || muscleIds.isEmpty()) {
            // Eğer eklenecek kas grubu yoksa, mevcutları silip true döner
            return deleteByExerciseId(exerciseId);
        }

        Connection connection = null;
        try {
            // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
            connection = DatabaseConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            // Önce mevcut ilişkileri sil
            deleteByExerciseId(exerciseId);

            // Yeni ilişkileri ekle
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_MUSCLE)) {
                for (Integer muscleId : muscleIds) {
                    if (muscleId != null) { // null kontrolü eklendi
                        statement.setInt(1, exerciseId);
                        statement.setInt(2, muscleId);
                        statement.addBatch();
                    }
                }

                int[] results = statement.executeBatch();
                connection.commit();

                System.out.println("✅ " + results.length + " egzersiz-kas ilişkisi başarıyla eklendi.");
                return true;
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
     * ResultSet'ten ExerciseMuscle nesnesini oluşturur
     */
    private ExerciseMuscle mapResultSetToExerciseMuscle(ResultSet resultSet) throws SQLException {
        ExerciseMuscle exerciseMuscle = new ExerciseMuscle();
        exerciseMuscle.setId(resultSet.getInt("id"));
        exerciseMuscle.setExerciseId(resultSet.getInt("exercise_id"));
        exerciseMuscle.setMuscleId(resultSet.getInt("muscle_id"));

        // İlişkili objeler
        try {
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(exerciseMuscle.getExerciseId());
                exercise.setName(exerciseName);
                exerciseMuscle.setExercise(exercise);
            }

            String muscleName = resultSet.getString("muscle_name");
            if (muscleName != null) {
                MuscleGroup muscleGroup = new MuscleGroup();
                muscleGroup.setId(exerciseMuscle.getMuscleId());
                muscleGroup.setName(muscleName);
                exerciseMuscle.setMuscleGroup(muscleGroup);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return exerciseMuscle;
    }
}