package com.example.selfworkout.dao;

import com.example.selfworkout.model.BodyStats;
import com.example.selfworkout.model.User;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BodyStats tablosu için Data Access Object sınıfı
 * Kullanıcı vücut istatistiklerini yönetir
 */
public class BodyStatsDAO {
    
    // SQL sorguları
    private static final String INSERT_BODY_STATS = 
        "INSERT INTO BodyStats (user_id, height, weight, record_date, notes) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT bs.*, u.username FROM BodyStats bs " +
        "LEFT JOIN Users u ON bs.user_id = u.id WHERE bs.id = ?";
    
    private static final String SELECT_BY_USER_ID = 
        "SELECT * FROM BodyStats WHERE user_id = ? ORDER BY record_date DESC";
    
    private static final String SELECT_LATEST_BY_USER = 
        "SELECT TOP 1 * FROM BodyStats WHERE user_id = ? ORDER BY record_date DESC";
    
    private static final String SELECT_BY_USER_AND_DATE_RANGE = 
        "SELECT * FROM BodyStats WHERE user_id = ? AND record_date BETWEEN ? AND ? ORDER BY record_date";
    
    private static final String UPDATE_BODY_STATS = 
        "UPDATE BodyStats SET height = ?, weight = ?, record_date = ?, notes = ? WHERE id = ?";
    
    private static final String DELETE_BODY_STATS = 
        "DELETE FROM BodyStats WHERE id = ?";
    
    private static final String COUNT_RECORDS_BY_USER = 
        "SELECT COUNT(*) FROM BodyStats WHERE user_id = ?";
    
    private static final String SELECT_WEIGHT_HISTORY = 
        "SELECT record_date, weight FROM BodyStats WHERE user_id = ? ORDER BY record_date";
    
    /**
     * Yeni vücut istatistiği ekler
     */
    public BodyStats save(BodyStats bodyStats) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_BODY_STATS, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setLong(1, bodyStats.getUserId());
            statement.setDouble(2, bodyStats.getHeight());
            statement.setDouble(3, bodyStats.getWeight());
            statement.setDate(4, Date.valueOf(bodyStats.getRecordDate()));
            statement.setString(5, bodyStats.getNotes());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Vücut istatistiği ekleme başarısız, hiçbir satır etkilenmedi.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bodyStats.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Vücut istatistiği başarıyla eklendi.");
                    return bodyStats;
                } else {
                    throw new SQLException("Vücut istatistiği ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }
    
    /**
     * ID'ye göre vücut istatistiği bulur
     */
    public BodyStats findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToBodyStats(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Kullanıcının tüm vücut istatistiklerini getirir
     */
    public List<BodyStats> findByUserId(int userId) throws SQLException {
        List<BodyStats> bodyStatsList = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bodyStatsList.add(mapResultSetToBodyStats(resultSet));
                }
            }
        }
        
        return bodyStatsList;
    }
    
    /**
     * Kullanıcının sınırlı sayıda vücut istatistiğini getirir
     */
    public List<BodyStats> findByUserId(int userId, int limit) throws SQLException {
        List<BodyStats> bodyStatsList = new ArrayList<>();
        String sql = SELECT_BY_USER_ID + " ORDER BY record_date DESC OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bodyStatsList.add(mapResultSetToBodyStats(resultSet));
                }
            }
        }
        
        return bodyStatsList;
    }
    
    /**
     * Kullanıcının en son vücut istatistiğini getirir
     */
    public BodyStats findLatestByUserId(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LATEST_BY_USER)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToBodyStats(resultSet);
                }
                return null;
            }
        }
    }
    
    /**
     * Belirli tarih aralığındaki vücut istatistiklerini getirir
     */
    public List<BodyStats> findByUserAndDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<BodyStats> bodyStatsList = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_AND_DATE_RANGE)) {
            
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bodyStatsList.add(mapResultSetToBodyStats(resultSet));
                }
            }
        }
        
        return bodyStatsList;
    }
    
    /**
     * Vücut istatistiğini günceller
     */
    public boolean update(BodyStats bodyStats) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BODY_STATS)) {
            
            statement.setDouble(1, bodyStats.getHeight());
            statement.setDouble(2, bodyStats.getWeight());
            statement.setDate(3, Date.valueOf(bodyStats.getRecordDate()));
            statement.setString(4, bodyStats.getNotes());
            statement.setInt(5, bodyStats.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Vücut istatistiği başarıyla güncellendi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Vücut istatistiğini siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BODY_STATS)) {
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Vücut istatistiği başarıyla silindi.");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Kullanıcının kaç tane kayıt olduğunu sayar
     */
    public int countRecordsByUserId(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_RECORDS_BY_USER)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }
    
    /**
     * Kullanıcının kilo geçmişini getirir (grafik için)
     */
    public List<BodyStats> getWeightHistory(int userId) throws SQLException {
        List<BodyStats> weightHistory = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_WEIGHT_HISTORY)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BodyStats stats = new BodyStats();
                    stats.setUserId(userId);
                    stats.setRecordDate(resultSet.getDate("record_date").toLocalDate());
                    stats.setWeight(resultSet.getDouble("weight"));
                    weightHistory.add(stats);
                }
            }
        }
        
        return weightHistory;
    }
    
    /**
     * Kullanıcının kilo değişimini hesaplar
     */
    public double calculateWeightChange(int userId, int daysPeriod) throws SQLException {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysPeriod);
        
        List<BodyStats> stats = findByUserAndDateRange(userId, startDate, endDate);
        
        if (stats.size() < 2) {
            return 0.0; // Yeterli veri yok
        }
        
        // En eski ve en yeni kayıtları al
        BodyStats oldest = stats.get(stats.size() - 1);
        BodyStats newest = stats.get(0);
        
        return newest.getWeight() - oldest.getWeight();
    }
    
    /**
     * Kullanıcının ortalama BMI'sını hesaplar (son 30 gün)
     */
    public double calculateAverageBMI(int userId) throws SQLException {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<BodyStats> stats = findByUserAndDateRange(userId, startDate, endDate);
        
        if (stats.isEmpty()) {
            return 0.0;
        }
        
        double totalBMI = stats.stream()
                .mapToDouble(BodyStats::calculateBMI)
                .sum();
        
        return totalBMI / stats.size();
    }
    
    /**
     * ResultSet'ten BodyStats nesnesini oluşturur
     */
    private BodyStats mapResultSetToBodyStats(ResultSet resultSet) throws SQLException {
        BodyStats bodyStats = new BodyStats();
        bodyStats.setId(resultSet.getInt("id"));
        bodyStats.setUserId(resultSet.getInt("user_id"));
        bodyStats.setHeight(resultSet.getDouble("height"));
        bodyStats.setWeight(resultSet.getDouble("weight"));
        bodyStats.setRecordDate(resultSet.getDate("record_date").toLocalDate());
        bodyStats.setNotes(resultSet.getString("notes"));
        
        // Eğer JOIN yapılmışsa user bilgisini de set et
        try {
            String username = resultSet.getString("username");
            if (username != null) {
                User user = new User();
                user.setId(resultSet.getInt("user_id"));
                user.setUsername(username);
                bodyStats.setUser(user);
            }
        } catch (SQLException e) {
            // Username kolonu yoksa, ignore et
        }
        
        return bodyStats;
    }
} 