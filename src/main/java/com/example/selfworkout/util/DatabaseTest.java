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
        boolean isConnected = DatabaseConnection.testConnection(); // DatabaseConnection içindeki static testConnection metodunu kullanır.

        if (isConnected) {
            System.out.println("✅ Database bağlantısı başarılı!");

            // Basit bir sorgu testi yapalım
            testBasicQuery();
        } else {
            System.out.println("❌ Database bağlantısı başarısız!");
            System.out.println("\n📋 Kontrol edilecek noktalar:");
            System.out.println("1. MSSQL Server çalışıyor mu?");
            System.out.println("2. 'selfworkout' database'i oluşturuldu mu?");
            System.out.println("3. database.properties dosyasındaki bilgiler doğru mu?");
            System.out.println("4. SQL Server Configuration Manager'dan TCP/IP bağlantıları aktif mi?");
            System.out.println("5. SQL Server Browser servisi çalışıyor mu?");
            System.out.println("6. Firewall ayarları engelliyor mu?");
        }

        // Uygulama kapanırken bağlantıyı kapat
        DatabaseConnection.closeConnection();
        System.out.println("\nTest tamamlandı.");
    }

    /**
     * Basit bir sorgu testi yapar
     */
    private static void testBasicQuery() {
        System.out.println("\n🔍 Temel sorgu testi yapılıyor...");

        // SQL Server için INFORMATION_SCHEMA.TABLES kullanıldı
        String testQuery = "SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_CATALOG = 'selfworkout'";

        // DÜZELTİLDİ: DatabaseConnection.getInstance().getConnection() olarak değiştirildi
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(testQuery)) {

            if (resultSet.next()) {
                int tableCount = resultSet.getInt("table_count");
                System.out.println("📊 'selfworkout' Database'inde " + tableCount + " tablo bulundu");

                if (tableCount >= 17) { // selfworkout.sql dosyasında 17 tablo var
                    System.out.println("✅ SelfWorkout tabloları mevcut ve dolu görünüyor!");
                } else {
                    System.out.println("⚠️  Tablo sayısı beklenenden az. SQL script'inizi çalıştırmayı veya eksik tabloları kontrol etmeyi unutmayın!");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Sorgu testi başarısız: " + e.getMessage());
            e.printStackTrace(); // Detaylı hata çıktısı için
        }
    }
}