package com.example.selfworkout.dao;

import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Equipments tablosu için Data Access Object sınıfı
 * Ekipman verilerini yönetir
 */
public class EquipmentDAO {

    // SQL sorguları
    private static final String INSERT_EQUIPMENT =
            "INSERT INTO Equipments (name, description) VALUES (?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT * FROM Equipments WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT * FROM Equipments ORDER BY name";

    private static final String SELECT_BY_NAME =
            "SELECT * FROM Equipments WHERE name = ?";

    private static final String UPDATE_EQUIPMENT =
            "UPDATE Equipments SET name = ?, description = ? WHERE id = ?";

    private static final String DELETE_EQUIPMENT =
            "DELETE FROM Equipments WHERE id = ?";

    /**
     * Yeni ekipman ekler
     */
    public Equipment save(Equipment equipment) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_EQUIPMENT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, equipment.getName());
            statement.setString(2, equipment.getDescription());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Ekipman ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    equipment.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Ekipman başarıyla eklendi: " + equipment.getName());
                    return equipment;
                } else {
                    throw new SQLException("Ekipman ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre ekipman bulur
     */
    public Equipment findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEquipment(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * İsme göre ekipman bulur
     */
    public Equipment findByName(String name) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEquipment(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Tüm ekipmanları getirir
     */
    public List<Equipment> findAll() throws SQLException {
        List<Equipment> equipments = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                equipments.add(mapResultSetToEquipment(resultSet));
            }
        }

        return equipments;
    }

    /**
     * Ekipmanı günceller
     */
    public boolean update(Equipment equipment) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_EQUIPMENT)) {

            statement.setString(1, equipment.getName());
            statement.setString(2, equipment.getDescription());
            statement.setInt(3, equipment.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Ekipman başarıyla güncellendi: " + equipment.getName());
                return true;
            }
            return false;
        }
    }

    /**
     * Ekipmanı siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_EQUIPMENT)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Ekipman başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * ResultSet'ten Equipment nesnesini oluşturur
     */
    private Equipment mapResultSetToEquipment(ResultSet resultSet) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(resultSet.getInt("id"));
        equipment.setName(resultSet.getString("name"));
        equipment.setDescription(resultSet.getString("description"));
        return equipment;
    }
}