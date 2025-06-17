package com.example.selfworkout.dao;

import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Role;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User tablosu için Data Access Object sınıfı
 * CRUD operasyonları ve authentication işlemlerini yönetir
 */
public class UserDAO {

    // SQL sorguları
    private static final String INSERT_USER =
            "INSERT INTO Users (role_id, username, email, password, name, surname, birthdate, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT u.*, r.role_name " +
                    "FROM Users u LEFT JOIN Roles r ON u.role_id = r.id WHERE u.id = ?";

    private static final String SELECT_ALL =
            "SELECT u.*, r.role_name " +
                    "FROM Users u LEFT JOIN Roles r ON u.role_id = r.id ORDER BY u.username";

    private static final String SELECT_BY_USERNAME =
            "SELECT u.*, r.role_name " +
                    "FROM Users u LEFT JOIN Roles r ON u.role_id = r.id WHERE u.username = ?";

    private static final String SELECT_BY_EMAIL =
            "SELECT u.*, r.role_name " +
                    "FROM Users u LEFT JOIN Roles r ON u.role_id = r.id WHERE u.email = ?";

    private static final String UPDATE_USER =
            "UPDATE Users SET role_id = ?, username = ?, email = ?, name = ?, surname = ?, birthdate = ? " +
                    "WHERE id = ?";

    private static final String UPDATE_PASSWORD =
            "UPDATE Users SET password = ? WHERE id = ?";

    private static final String DELETE_USER =
            "DELETE FROM Users WHERE id = ?";

    private static final String CHECK_USERNAME_EXISTS =
            "SELECT COUNT(*) FROM Users WHERE username = ? AND id != ?";

    private static final String CHECK_EMAIL_EXISTS =
            "SELECT COUNT(*) FROM Users WHERE email = ? AND id != ?";

    /**
     * Yeni kullanıcı ekler
     */
    public User save(User user) throws SQLException {
        // Kullanıcı adı ve email kontrolü
        // DÜZELTİLDİ: 'this.' ön eki eklendi
        if (this.isUsernameExists(user.getUsername(), 0)) {
            throw new SQLException("Bu kullanıcı adı zaten kullanılıyor: " + user.getUsername());
        }

        // DÜZELTİLDİ: 'this.' ön eki eklendi
        if (this.isEmailExists(user.getEmail(), 0)) {
            throw new SQLException("Bu email adresi zaten kullanılıyor: " + user.getEmail());
        }

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, user.getRoleId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword()); // Şifre hashleme daha sonra eklenecek
            statement.setString(5, user.getName());
            statement.setString(6, user.getSurname());

            if (user.getBirthdate() != null) {
                statement.setDate(7, Date.valueOf(user.getBirthdate()));
            } else {
                statement.setNull(7, Types.DATE);
            }

            // Created_at otomatik set edilsin
            statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Kullanıcı ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            // Oluşturulan ID'yi al
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    user.setCreatedAt(LocalDateTime.now());
                    System.out.println("✅ Kullanıcı başarıyla eklendi: " + user.getUsername());
                    return user;
                } else {
                    throw new SQLException("Kullanıcı ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre kullanıcı bulur (Role bilgisi ile birlikte)
     */
    public User findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Kullanıcı adına göre bulur (Login için)
     */
    public User findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Email'e göre bulur
     */
    public User findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Tüm kullanıcıları getirir
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }

        return users;
    }

    /**
     * Kullanıcı bilgilerini günceller (şifre hariç)
     */
    public boolean update(User user) throws SQLException {
        // Kullanıcı adı ve email kontrolü (kendi ID'si hariç)
        // DÜZELTİLDİ: 'this.' ön eki eklendi
        if (this.isUsernameExists(user.getUsername(), user.getId())) {
            throw new SQLException("Bu kullanıcı adı zaten kullanılıyor: " + user.getUsername());
        }

        // DÜZELTİLDİ: 'this.' ön eki eklendi
        if (this.isEmailExists(user.getEmail(), user.getId())) {
            throw new SQLException("Bu email adresi zaten kullanılıyor: " + user.getEmail());
        }

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {

            statement.setInt(1, user.getRoleId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getName());
            statement.setString(5, user.getSurname());

            if (user.getBirthdate() != null) {
                statement.setDate(6, Date.valueOf(user.getBirthdate()));
            } else {
                statement.setNull(6, Types.DATE);
            }

            statement.setInt(7, user.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Kullanıcı başarıyla güncellendi: " + user.getUsername());
                return true;
            }
            return false;
        }
    }

    /**
     * Kullanıcı şifresini günceller
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD)) {

            statement.setString(1, newPassword); // Şifre hashleme daha sonra eklenecek
            statement.setInt(2, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Kullanıcı şifresi başarıyla güncellendi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Kullanıcıyı siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Kullanıcı başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * Authentication - Kullanıcı adı ve şifre kontrolü
     */
    public User authenticate(String username, String password) throws SQLException {
        try {
            User user = findByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                System.out.println("✅ Database'den giriş başarılı: " + username);
                return user;
            }

            return null;

        } catch (SQLException e) {
            // Veritabanı bağlantısı yoksa veya sorgu hatası varsa, geçici olarak hardcoded admin kullanıcısı
            System.err.println("⚠️ Veritabanı bağlantısı veya sorgu hatası: " + e.getMessage() + ". Geçici admin kontrolü yapılıyor...");

            if ("admin".equals(username) && "admin123".equals(password)) {
                // Geçici admin kullanıcısı oluştur
                User adminUser = new User();
                adminUser.setId(1);
                adminUser.setRoleId(1);
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@selfworkout.com");
                adminUser.setPassword("admin123");
                adminUser.setName("System");
                adminUser.setSurname("Administrator");
                adminUser.setCreatedAt(LocalDateTime.now().minusDays(30)); // Kayıt tarihi simülasyonu

                // Admin rolü oluştur
                Role adminRole = new Role();
                adminRole.setId(1);
                adminRole.setRoleName("admin");
                adminRole.setDescription("Sistem yöneticisi");
                adminUser.setRole(adminRole);

                System.out.println("✅ Geçici admin girişi başarılı: " + username);
                return adminUser;
            }

            System.out.println("❌ Geçici giriş başarısız: " + username);
            throw e; // Orijinal SQLException'ı tekrar fırlat
        }
    }

    /**
     * Kullanıcı adının kullanılıp kullanılmadığını kontrol eder
     */
    public boolean isUsernameExists(String username, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_USERNAME_EXISTS)) {

            statement.setString(1, username);
            statement.setInt(2, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Email'in kullanılıp kullanılmadığını kontrol eder
     */
    public boolean isEmailExists(String email, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_EMAIL_EXISTS)) {

            statement.setString(1, email);
            statement.setInt(2, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * ResultSet'ten User nesnesini oluşturur (Role bilgisi ile birlikte)
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setRoleId(resultSet.getInt("role_id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setName(resultSet.getString("name"));
        user.setSurname(resultSet.getString("surname"));

        // Birthdate null olabilir
        Date birthDate = resultSet.getDate("birthdate");
        if (birthDate != null) {
            user.setBirthdate(birthDate.toLocalDate());
        }

        // Created_at null olabilir
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Role bilgisini set et
        String roleName = resultSet.getString("role_name");
        if (roleName != null) {
            Role role = new Role();
            role.setId(resultSet.getInt("role_id"));
            role.setRoleName(roleName);
            role.setDescription(""); // Basit bir açıklama koy ya da null bırak
            user.setRole(role);
        }

        return user;
    }
}