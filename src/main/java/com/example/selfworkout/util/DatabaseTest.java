package com.example.selfworkout.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database bağlantısını test etmek için utility sınıfı
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("🔌 Database Bağlantı Testi Başlatılıyor...\n");
        
        // Bağlantı bilgilerini yazdır
        DatabaseConnection.printConnectionInfo();
        
        // Bağlantıyı test et
        System.out.println("\n🧪 Bağlantı testi yapılıyor...");
        boolean isConnected = DatabaseConnection.testConnection();
        
        if (isConnected) {
            System.out.println("✅ Database bağlantısı başarılı!");
            
            // Basit bir sorgu testi yapalım
            testBasicQuery();
        } else {
            System.out.println("❌ Database bağlantısı başarısız!");
            System.out.println("\n📋 Kontrol edilecek noktalar:");
            System.out.println("1. MSSQL Server çalışıyor mu?");
            System.out.println("2. selfworkout database'i oluşturuldu mu?");
            System.out.println("3. database.properties dosyasındaki bilgiler doğru mu?");
            System.out.println("4. SQL Server'da TCP/IP bağlantıları aktif mi?");
        }
    }
    
    /**
     * Basit bir sorgu testi yapar
     */
    private static void testBasicQuery() {
        System.out.println("\n🔍 Temel sorgu testi yapılıyor...");
        
        String testQuery = "SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'";
        
        try (Connection connection = DatabaseConnection.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(testQuery)) {
            
            if (resultSet.next()) {
                int tableCount = resultSet.getInt("table_count");
                System.out.println("📊 Database'de " + tableCount + " tablo bulundu");
                
                if (tableCount >= 17) {
                    System.out.println("✅ SelfWorkout tabloları mevcut görünüyor!");
                } else {
                    System.out.println("⚠️  SQL script'inizi çalıştırmayı unutmayın!");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Sorgu testi başarısız: " + e.getMessage());
        }
    }
} 