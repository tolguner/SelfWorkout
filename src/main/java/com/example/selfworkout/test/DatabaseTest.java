package com.example.selfworkout.test;

import com.example.selfworkout.util.DatabaseConnection;

/**
 * Veritabanı bağlantısını test etmek için test sınıfı
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 SelfWorkout - Veritabanı Bağlantı Testi");
        System.out.println("==========================================");
        
        // 1. Bağlantı bilgilerini göster
        System.out.println("\n📋 Bağlantı Bilgileri:");
        DatabaseConnection.printConnectionInfo();
        
        // 2. Bağlantı testi
        System.out.println("\n🔍 Bağlantı Testi:");
        boolean connectionTest = DatabaseConnection.testConnection();
        
        if (connectionTest) {
            System.out.println("✅ Bağlantı başarılı!");
            System.out.println("\n💡 Veritabanı kurulumu manuel olarak yapılmalıdır.");
            System.out.println("📖 MSSQL_Database_Setup.md dosyasını kontrol edin.");
            System.out.println("👤 Admin giriş bilgileri:");
            System.out.println("   Kullanıcı Adı: admin");
            System.out.println("   Şifre: admin123");
            
        } else {
            System.out.println("❌ Bağlantı başarısız!");
            System.out.println("\n💡 Kontrol Edilecekler:");
            System.out.println("1. MSSQL Server çalışıyor mu?");
            System.out.println("2. Veritabanı 'selfworkout' oluşturuldu mu?");
            System.out.println("3. database.properties dosyasındaki bilgiler doğru mu?");
            System.out.println("4. SQL Server Authentication aktif mi?");
        }
        
        System.out.println("\n==========================================");
        System.out.println("Test tamamlandı.");
    }
} 