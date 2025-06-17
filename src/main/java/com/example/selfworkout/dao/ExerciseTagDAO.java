package com.example.selfworkout.dao;

import com.example.selfworkout.model.ExerciseTag;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseTags tablosu için Data Access Object sınıfı
 * Egzersiz etiketlerini yönetir
 */
public class ExerciseTagDAO {

    // SQL sorguları
    private static final String INSERT_EXERCISE_TAG =
            "INSERT INTO ExerciseTags (exercise_id, tag) VALUES (?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT et.*, e.name as exercise_name " +
                    "FROM ExerciseTags et " +
                    "LEFT JOIN Exercises e ON et.exercise_id = e.id " +
                    "WHERE et.id = ?";

    private static final String SELECT_BY_EXERCISE_ID =
            "SELECT et.*, e.name as exercise_name " +
                    "FROM ExerciseTags et " +
                    "LEFT JOIN Exercises e ON et.exercise_id = e.id " +
                    "WHERE et.exercise_id = ?";

    private static final String SELECT_BY_TAG =
            "SELECT et.*, e.name as exercise_name " +
                    "FROM ExerciseTags et " +
                    "LEFT JOIN Exercises e ON et.exercise_id = e.id " +
                    "WHERE et.tag LIKE ?";

    private static final String SELECT_ALL =
            "SELECT et.*, e.name as exercise_name " +
                    "FROM ExerciseTags et " +
                    "LEFT JOIN Exercises e ON et.exercise_id = e.id " +
                    "ORDER BY e.name, et.tag";

    private static final String SELECT_DISTINCT_TAGS =
            "SELECT DISTINCT tag FROM ExerciseTags ORDER BY tag";

    private static final String DELETE_EXERCISE_TAG =
            "DELETE FROM ExerciseTags WHERE id = ?";

    private static final String DELETE_BY_EXERCISE_ID =
            "DELETE FROM ExerciseTags WHERE exercise_id = ?";

    private static final String DELETE_BY_TAG =
            "DELETE FROM ExerciseTags WHERE tag = ?";

    private static final String CHECK_EXISTS =
            "SELECT COUNT(*) FROM ExerciseTags WHERE exercise_id = ? AND tag = ?";

    /**
     * Yeni egzersiz etiketi ekler
     */
    public ExerciseTag save(ExerciseTag exerciseTag) throws SQLException {
        // Önce aynı etiket var mı kontrol et
        if (exists(exerciseTag.getExerciseId(), exerciseTag.getTag())) {
            System.out.println("⚠️ Bu egzersiz etiketi zaten mevcut!");
            return null;
        }

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_TAG, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exerciseTag.getExerciseId());
            statement.setString(2, exerciseTag.getTag().trim().toLowerCase());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Egzersiz etiketi ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exerciseTag.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Egzersiz etiketi başarıyla eklendi: " + exerciseTag.getTag());
                    return exerciseTag;
                } else {
                    throw new SQLException("Egzersiz etiketi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre egzersiz etiketi bulur
     */
    public ExerciseTag findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToExerciseTag(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Egzersiz ID'sine göre etiketleri getirir
     */
    public List<ExerciseTag> findByExerciseId(int exerciseId) throws SQLException {
        List<ExerciseTag> exerciseTags = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseTags.add(mapResultSetToExerciseTag(resultSet));
                }
            }
        }

        return exerciseTags;
    }

    /**
     * Etikete göre egzersizleri arar (LIKE ile)
     */
    public List<ExerciseTag> findByTag(String tag) throws SQLException {
        List<ExerciseTag> exerciseTags = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_TAG)) {

            statement.setString(1, "%" + tag.trim().toLowerCase() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseTags.add(mapResultSetToExerciseTag(resultSet));
                }
            }
        }

        return exerciseTags;
    }

    /**
     * Tüm egzersiz etiketlerini getirir
     */
    public List<ExerciseTag> findAll() throws SQLException {
        List<ExerciseTag> exerciseTags = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                exerciseTags.add(mapResultSetToExerciseTag(resultSet));
            }
        }

        return exerciseTags;
    }

    /**
     * Tüm benzersiz etiketleri getirir
     */
    public List<String> findAllDistinctTags() throws SQLException {
        List<String> tags = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DISTINCT_TAGS);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tags.add(resultSet.getString("tag"));
            }
        }

        return tags;
    }

    /**
     * Egzersiz etiketini siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_TAG)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersiz etiketi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersizin tüm etiketlerini siler
     */
    public boolean deleteByExerciseId(int exerciseId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersizin tüm etiketleri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli etiketi tüm egzersizlerden siler
     */
    public boolean deleteByTag(String tag) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_TAG)) {

            statement.setString(1, tag.trim().toLowerCase());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ '" + tag + "' etiketi tüm egzersizlerden silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersiz-etiket kombinasyonunun var olup olmadığını kontrol eder
     */
    public boolean exists(int exerciseId, String tag) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_EXISTS)) {

            statement.setInt(1, exerciseId);
            statement.setString(2, tag.trim().toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Toplu egzersiz etiketi ekleme
     */
    public boolean saveAll(int exerciseId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            // Eğer eklenecek tag yoksa, mevcutları silip true döner
            return deleteByExerciseId(exerciseId);
        }

        Connection connection = null;
        try {
            // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
            connection = DatabaseConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            // Önce mevcut etiketleri sil
            deleteByExerciseId(exerciseId);

            // Yeni etiketleri ekle
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_TAG)) {
                for (String tag : tags) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        statement.setInt(1, exerciseId);
                        statement.setString(2, tag.trim().toLowerCase());
                        statement.addBatch();
                    }
                }

                int[] results = statement.executeBatch();
                connection.commit();

                System.out.println("✅ " + results.length + " egzersiz etiketi başarıyla eklendi.");
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
     * Etiket arama (autocomplete için)
     */
    public List<String> searchTags(String searchTerm, int limit) throws SQLException {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT DISTINCT tag FROM ExerciseTags WHERE tag LIKE ? ORDER BY tag";

        if (limit > 0) {
            // MSSQL için TOP N kullanımı
            sql = "SELECT TOP " + limit + " DISTINCT tag FROM ExerciseTags WHERE tag LIKE ? ORDER BY tag";
        }

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + searchTerm.trim().toLowerCase() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tags.add(resultSet.getString("tag"));
                }
            }
        }

        return tags;
    }

    /**
     * ResultSet'ten ExerciseTag nesnesini oluşturur
     */
    private ExerciseTag mapResultSetToExerciseTag(ResultSet resultSet) throws SQLException {
        ExerciseTag exerciseTag = new ExerciseTag();
        exerciseTag.setId(resultSet.getInt("id"));
        exerciseTag.setExerciseId(resultSet.getInt("exercise_id"));
        exerciseTag.setTag(resultSet.getString("tag"));

        // İlişkili egzersiz objesi
        try {
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(exerciseTag.getExerciseId());
                exercise.setName(exerciseName);
                exerciseTag.setExercise(exercise);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return exerciseTag;
    }
}