package com.example.selfworkout.dao;

import com.example.selfworkout.model.Log;
import com.example.selfworkout.model.User;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs tablosu için Data Access Object sınıfı
 * Sistem loglarını yönetir
 */
public class LogDAO {

    // SQL sorguları
    private static final String INSERT_LOG =
            "INSERT INTO Logs (user_id, action, description, created_at) VALUES (?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT l.*, u.username " +
                    "FROM Logs l " +
                    "LEFT JOIN Users u ON l.user_id = u.id " +
                    "WHERE l.id = ?";

    private static final String SELECT_BY_USER_ID =
            "SELECT l.*, u.username " +
                    "FROM Logs l " +
                    "LEFT JOIN Users u ON l.user_id = u.id " +
                    "WHERE l.user_id = ? ORDER BY l.created_at DESC";

    private static final String SELECT_ALL =
            "SELECT l.*, u.username " +
                    "FROM Logs l " +
                    "LEFT JOIN Users u ON l.user_id = u.id " +
                    "ORDER BY l.created_at DESC";

    private static final String DELETE_LOG =
            "DELETE FROM Logs WHERE id = ?";

    /**
     * Yeni log kaydı ekler
     */
    public Log save(Log log) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_LOG, Statement.RETURN_GENERATED_KEYS)) {

            // user_id NULL olabilir
            if (log.getUserId() != null) {
                statement.setInt(1, log.getUserId());
            } else {
                statement.setNull(1, Types.INTEGER);
            }

            statement.setString(2, log.getAction());
            statement.setString(3, log.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Log kaydı ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setId(generatedKeys.getInt(1));
                    log.setTimestamp(LocalDateTime.now());
                    return log;
                } else {
                    throw new SQLException("Log kaydı ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre log kaydı bulur
     */
    public Log findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToLog(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Kullanıcı ID'sine göre log kayıtlarını getirir
     */
    public List<Log> findByUserId(int userId) throws SQLException {
        List<Log> logs = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToLog(resultSet));
                }
            }
        }

        return logs;
    }

    /**
     * Tüm log kayıtlarını getirir
     */
    public List<Log> findAll() throws SQLException {
        List<Log> logs = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapResultSetToLog(resultSet));
            }
        }

        return logs;
    }

    /**
     * Log kaydını siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_LOG)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        }
    }

    /**
     * Hızlı log ekleme metodu
     * NOT: Bu metod, DAO'nun sorumluluğundan çok Service katmanına aittir.
     * LogService sınıfında 'createLog' metodunu kullanmak daha iyidir.
     */
    @Deprecated // Bu metodun kullanımı yerine LogService'in createLog metodu önerilir.
    public void logAction(int userId, String action, String description) {
        try {
            Log log = new Log();
            log.setUserId(userId);
            log.setAction(action);
            log.setDescription(description);
            save(log);
        } catch (SQLException e) {
            System.err.println("Log kaydı eklenirken hata: " + e.getMessage());
        }
    }

    /**
     * ResultSet'ten Log nesnesini oluşturur
     */
    private Log mapResultSetToLog(ResultSet resultSet) throws SQLException {
        Log log = new Log();
        log.setId(resultSet.getInt("id"));

        // user_id NULL olabilir (sistem logları için)
        try {
            int userId = resultSet.getInt("user_id");
            if (!resultSet.wasNull()) {
                log.setUserId(userId);
            }
        } catch (SQLException e) {
            // Ignore - user_id may not exist in this specific ResultSet (if no JOIN)
            // Or if user_id column simply isn't in the result set due to query design.
        }

        log.setAction(resultSet.getString("action"));
        log.setDescription(resultSet.getString("description"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            log.setTimestamp(createdAt.toLocalDateTime());
        }

        // İlişkili kullanıcı objesi
        try {
            String username = resultSet.getString("username");
            if (username != null) {
                User user = new User();
                user.setId(log.getUserId() != null ? log.getUserId() : 0); // Kullanıcı ID'si null olabilir
                user.setUsername(username);
                log.setUser(user);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return log;
    }
}