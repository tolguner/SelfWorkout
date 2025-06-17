package com.example.selfworkout.dao;

import com.example.selfworkout.model.ExerciseEquipment;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseEquipments tablosu için Data Access Object sınıfı
 * Egzersiz-ekipman ilişkilerini yönetir
 */
public class ExerciseEquipmentDAO {

    // SQL sorguları
    private static final String INSERT_EXERCISE_EQUIPMENT =
            "INSERT INTO ExerciseEquipments (exercise_id, equipment_id) VALUES (?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT ee.*, e.name as exercise_name, eq.name as equipment_name " +
                    "FROM ExerciseEquipments ee " +
                    "LEFT JOIN Exercises e ON ee.exercise_id = e.id " +
                    "LEFT JOIN Equipments eq ON ee.equipment_id = eq.id " +
                    "WHERE ee.id = ?";

    private static final String SELECT_BY_EXERCISE_ID =
            "SELECT ee.*, e.name as exercise_name, eq.name as equipment_name " +
                    "FROM ExerciseEquipments ee " +
                    "LEFT JOIN Exercises e ON ee.exercise_id = e.id " +
                    "LEFT JOIN Equipments eq ON ee.equipment_id = eq.id " +
                    "WHERE ee.exercise_id = ?";

    private static final String SELECT_BY_EQUIPMENT_ID =
            "SELECT ee.*, e.name as exercise_name, eq.name as equipment_name " +
                    "FROM ExerciseEquipments ee " +
                    "LEFT JOIN Exercises e ON ee.exercise_id = e.id " +
                    "LEFT JOIN Equipments eq ON ee.equipment_id = eq.id " +
                    "WHERE ee.equipment_id = ?";

    private static final String SELECT_ALL =
            "SELECT ee.*, e.name as exercise_name, eq.name as equipment_name " +
                    "FROM ExerciseEquipments ee " +
                    "LEFT JOIN Exercises e ON ee.exercise_id = e.id " +
                    "LEFT JOIN Equipments eq ON ee.equipment_id = eq.id " +
                    "ORDER BY e.name, eq.name";

    private static final String DELETE_EXERCISE_EQUIPMENT =
            "DELETE FROM ExerciseEquipments WHERE id = ?";

    private static final String DELETE_BY_EXERCISE_ID =
            "DELETE FROM ExerciseEquipments WHERE exercise_id = ?";

    private static final String DELETE_BY_EQUIPMENT_ID =
            "DELETE FROM ExerciseEquipments WHERE equipment_id = ?";

    private static final String CHECK_EXISTS =
            "SELECT COUNT(*) FROM ExerciseEquipments WHERE exercise_id = ? AND equipment_id = ?";

    /**
     * Yeni egzersiz-ekipman ilişkisi ekler
     */
    public ExerciseEquipment save(ExerciseEquipment exerciseEquipment) throws SQLException {
        // Önce aynı ilişki var mı kontrol et
        if (exists(exerciseEquipment.getExerciseId(), exerciseEquipment.getEquipmentId())) {
            System.out.println("⚠️ Bu egzersiz-ekipman ilişkisi zaten mevcut!");
            return null;
        }

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_EQUIPMENT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exerciseEquipment.getExerciseId());
            statement.setInt(2, exerciseEquipment.getEquipmentId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Egzersiz-ekipman ilişkisi ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exerciseEquipment.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Egzersiz-ekipman ilişkisi başarıyla eklendi.");
                    return exerciseEquipment;
                } else {
                    throw new SQLException("Egzersiz-ekipman ilişkisi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre egzersiz-ekipman ilişkisi bulur
     */
    public ExerciseEquipment findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToExerciseEquipment(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Egzersiz ID'sine göre ekipmanları getirir
     */
    public List<ExerciseEquipment> findByExerciseId(int exerciseId) throws SQLException {
        List<ExerciseEquipment> exerciseEquipments = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseEquipments.add(mapResultSetToExerciseEquipment(resultSet));
                }
            }
        }

        return exerciseEquipments;
    }

    /**
     * Ekipman ID'sine göre egzersizleri getirir
     */
    public List<ExerciseEquipment> findByEquipmentId(int equipmentId) throws SQLException {
        List<ExerciseEquipment> exerciseEquipments = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EQUIPMENT_ID)) {

            statement.setInt(1, equipmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exerciseEquipments.add(mapResultSetToExerciseEquipment(resultSet));
                }
            }
        }

        return exerciseEquipments;
    }

    /**
     * Tüm egzersiz-ekipman ilişkilerini getirir
     */
    public List<ExerciseEquipment> findAll() throws SQLException {
        List<ExerciseEquipment> exerciseEquipments = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                exerciseEquipments.add(mapResultSetToExerciseEquipment(resultSet));
            }
        }

        return exerciseEquipments;
    }

    /**
     * Egzersiz-ekipman ilişkisini siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_EXERCISE_EQUIPMENT)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersiz-ekipman ilişkisi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersizin tüm ekipman ilişkilerini siler
     */
    public boolean deleteByExerciseId(int exerciseId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_EXERCISE_ID)) {

            statement.setInt(1, exerciseId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Egzersizin tüm ekipman ilişkileri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli ekipmanın tüm egzersiz ilişkilerini siler
     */
    public boolean deleteByEquipmentId(int equipmentId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_EQUIPMENT_ID)) {

            statement.setInt(1, equipmentId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Ekipmanın tüm egzersiz ilişkileri silindi. Silinen: " + affectedRows);
                return true;
            }
            return false;
        }
    }

    /**
     * Belirli egzersiz-ekipman ilişkisinin var olup olmadığını kontrol eder
     */
    public boolean exists(int exerciseId, int equipmentId) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_EXISTS)) {

            statement.setInt(1, exerciseId);
            statement.setInt(2, equipmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Toplu egzersiz-ekipman ilişkisi ekleme
     */
    public boolean saveAll(int exerciseId, List<Integer> equipmentIds) throws SQLException {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            // Eğer eklenecek ekipman yoksa, mevcutları silip true döner
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
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXERCISE_EQUIPMENT)) {
                for (Integer equipmentId : equipmentIds) {
                    statement.setInt(1, exerciseId);
                    statement.setInt(2, equipmentId);
                    statement.addBatch();
                }

                int[] results = statement.executeBatch();
                connection.commit();

                System.out.println("✅ " + results.length + " egzersiz-ekipman ilişkisi başarıyla eklendi.");
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
     * ResultSet'ten ExerciseEquipment nesnesini oluşturur
     */
    private ExerciseEquipment mapResultSetToExerciseEquipment(ResultSet resultSet) throws SQLException {
        ExerciseEquipment exerciseEquipment = new ExerciseEquipment();
        exerciseEquipment.setId(resultSet.getInt("id"));
        exerciseEquipment.setExerciseId(resultSet.getInt("exercise_id"));
        exerciseEquipment.setEquipmentId(resultSet.getInt("equipment_id"));

        // İlişkili objeler
        try {
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(exerciseEquipment.getExerciseId());
                exercise.setName(exerciseName);
                exerciseEquipment.setExercise(exercise);
            }

            String equipmentName = resultSet.getString("equipment_name");
            if (equipmentName != null) {
                Equipment equipment = new Equipment();
                equipment.setId(exerciseEquipment.getEquipmentId());
                equipment.setName(equipmentName);
                exerciseEquipment.setEquipment(equipment);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return exerciseEquipment;
    }
}