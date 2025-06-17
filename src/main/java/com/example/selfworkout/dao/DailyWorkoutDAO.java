package com.example.selfworkout.dao;

import com.example.selfworkout.model.DailyWorkout;
import com.example.selfworkout.model.User;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DailyWorkouts tablosu için Data Access Object sınıfı
 * Günlük antrenmanları yönetir
 */
public class DailyWorkoutDAO {

    // SQL sorguları
    private static final String INSERT_DAILY_WORKOUT =
            "INSERT INTO DailyWorkouts (user_id, workout_date, total_duration, notes, created_at) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT dw.*, u.username " +
                    "FROM DailyWorkouts dw " +
                    "LEFT JOIN Users u ON dw.user_id = u.id " +
                    "WHERE dw.id = ?";

    private static final String SELECT_BY_USER_ID =
            "SELECT dw.*, u.username " +
                    "FROM DailyWorkouts dw " +
                    "LEFT JOIN Users u ON dw.user_id = u.id " +
                    "WHERE dw.user_id = ? ORDER BY dw.workout_date DESC";

    private static final String SELECT_BY_DATE_RANGE =
            "SELECT dw.*, u.username " +
                    "FROM DailyWorkouts dw " +
                    "LEFT JOIN Users u ON dw.user_id = u.id " +
                    "WHERE dw.user_id = ? AND dw.workout_date BETWEEN ? AND ? ORDER BY dw.workout_date DESC";

    private static final String UPDATE_DAILY_WORKOUT =
            "UPDATE DailyWorkouts SET total_duration = ?, notes = ? WHERE id = ?";

    private static final String DELETE_DAILY_WORKOUT =
            "DELETE FROM DailyWorkouts WHERE id = ?";

    /**
     * Yeni günlük antrenman ekler
     */
    public DailyWorkout save(DailyWorkout dailyWorkout) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_DAILY_WORKOUT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, dailyWorkout.getUserId());
            statement.setDate(2, Date.valueOf(dailyWorkout.getWorkoutDate()));
            statement.setInt(3, dailyWorkout.getTotalDuration());
            statement.setString(4, dailyWorkout.getNotes());
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Günlük antrenman ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dailyWorkout.setId(generatedKeys.getInt(1));
                    dailyWorkout.setCreatedAt(LocalDateTime.now());
                    System.out.println("✅ Günlük antrenman başarıyla eklendi.");
                    return dailyWorkout;
                } else {
                    throw new SQLException("Günlük antrenman ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre günlük antrenman bulur
     */
    public DailyWorkout findById(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDailyWorkout(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Kullanıcı ID'sine göre günlük antrenmanları getirir
     */
    public List<DailyWorkout> findByUserId(int userId) throws SQLException {
        List<DailyWorkout> dailyWorkouts = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dailyWorkouts.add(mapResultSetToDailyWorkout(resultSet));
                }
            }
        }

        return dailyWorkouts;
    }

    /**
     * Tarih aralığına göre günlük antrenmanları getirir
     */
    public List<DailyWorkout> findByDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<DailyWorkout> dailyWorkouts = new ArrayList<>();

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_DATE_RANGE)) {

            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dailyWorkouts.add(mapResultSetToDailyWorkout(resultSet));
                }
            }
        }

        return dailyWorkouts;
    }

    /**
     * Günlük antrenmanı günceller
     */
    public boolean update(DailyWorkout dailyWorkout) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_DAILY_WORKOUT)) {

            statement.setInt(1, dailyWorkout.getTotalDuration());
            statement.setString(2, dailyWorkout.getNotes());
            statement.setInt(3, dailyWorkout.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Günlük antrenman başarıyla güncellendi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Günlük antrenmanı siler
     */
    public boolean delete(int id) throws SQLException {
        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_DAILY_WORKOUT)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Günlük antrenman başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * ResultSet'ten DailyWorkout nesnesini oluşturur
     */
    private DailyWorkout mapResultSetToDailyWorkout(ResultSet resultSet) throws SQLException {
        DailyWorkout dailyWorkout = new DailyWorkout();
        dailyWorkout.setId(resultSet.getInt("id"));
        dailyWorkout.setUserId(resultSet.getInt("user_id"));

        Date workoutDate = resultSet.getDate("workout_date");
        if (workoutDate != null) {
            dailyWorkout.setWorkoutDate(workoutDate.toLocalDate());
        }

        dailyWorkout.setTotalDuration(resultSet.getInt("total_duration"));
        dailyWorkout.setNotes(resultSet.getString("notes"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            dailyWorkout.setCreatedAt(createdAt.toLocalDateTime());
        }

        // İlişkili kullanıcı objesi
        try {
            // ResultSet'te 'username' kolonu yoksa SQLException fırlatır, bu yüzden try-catch
            String username = resultSet.getString("username");
            if (username != null) {
                User user = new User();
                user.setId(dailyWorkout.getUserId());
                user.setUsername(username);
                dailyWorkout.setUser(user);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return dailyWorkout;
    }
}