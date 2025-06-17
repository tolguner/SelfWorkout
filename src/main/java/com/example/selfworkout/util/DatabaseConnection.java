package com.example.selfworkout.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final Properties properties;

    private DatabaseConnection() {
        properties = new Properties();
        loadProperties();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            // Thread-safe singleton initialization
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                // Eğer resources/database.properties bulunamazsa logla
                System.err.println("❌ Hata: 'database.properties' dosyası bulunamadı. Lütfen 'src/main/resources' altında olduğundan emin olun.");
                throw new IOException("Unable to find database.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("❌ Hata: 'database.properties' yüklenirken sorun oluştu: " + e.getMessage());
            e.printStackTrace();
            // Uygulamanın çalışmasını engellemek için runtime exception fırlatılabilir
            // throw new RuntimeException("Failed to load database properties", e);
        }
    }

    public Connection getConnection() throws SQLException {
        // Eğer bağlantı null ise veya kapalı ise yeni bir bağlantı oluştur
        if (connection == null || connection.isClosed()) {
            try {
                // JDBC sürücüsünü yükle (Maven pom.xml'de tanımlı olması gerekir)
                Class.forName(properties.getProperty("db.driver"));

                connection = DriverManager.getConnection(
                        properties.getProperty("db.url"),
                        properties.getProperty("db.username"), // properties.getProperty("db.user") yerine username kullanıldı
                        properties.getProperty("db.password")
                );
                System.out.println("✅ Veritabanı bağlantısı kuruldu.");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ JDBC sürücüsü bulunamadı: " + e.getMessage());
                throw new SQLException("JDBC sürücüsü bulunamadı. Lütfen 'mssql-jdbc' bağımlılığının doğru olduğundan emin olun.");
            } catch (SQLException e) {
                System.err.println("❌ Veritabanı bağlantısı başarısız oldu: " + e.getMessage());
                throw e; // Bağlantı hatasını daha yukarı fırlat
            }
        }
        return connection;
    }

    /**
     * Veritabanı bağlantısını test eder.
     */
    public static boolean testConnection() {
        try (Connection conn = getInstance().getConnection()) { // Doğru kullanım: getInstance() üzerinden çağrı
            return true;
        } catch (SQLException e) {
            // Bağlantı testi başarısız olduğunda detaylı hata mesajı gösterir.
            System.err.println("❌ Veritabanı bağlantı testi başarısız oldu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bağlantı bilgilerini konsola yazdırır (debugging için).
     */
    public static void printConnectionInfo() {
        System.out.println("Veritabanı URL: " + getInstance().properties.getProperty("db.url"));
        System.out.println("Veritabanı Kullanıcı: " + getInstance().properties.getProperty("db.username")); // 'user' yerine 'username'
        System.out.println("Veritabanı Sürücü: " + getInstance().properties.getProperty("db.driver"));
    }

    /**
     * Uygulama kapatılırken bağlantıyı kapatır.
     */
    public static void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                if (!instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("🔌 Veritabanı bağlantısı kapatıldı.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Veritabanı bağlantısı kapatılırken hata: " + e.getMessage());
            } finally {
                instance.connection = null;
                instance = null; // Singleton instance'ı sıfırla
            }
        }
    }
}