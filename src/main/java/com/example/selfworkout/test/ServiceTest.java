package com.example.selfworkout.test;

import com.example.selfworkout.service.ServiceManager;
import com.example.selfworkout.service.AuthenticationService;
import com.example.selfworkout.service.UserService;
import com.example.selfworkout.service.ExerciseService;
import com.example.selfworkout.service.StatisticsService;

/**
 * Service katmanını test etmek için basit test sınıfı
 */
public class ServiceTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Service Layer Test Başlatılıyor...\n");
        
        try {
            // ServiceManager'ı başlat
            ServiceManager serviceManager = ServiceManager.getInstance();
            serviceManager.printSystemInfo();
            
            // Servisleri al
            AuthenticationService authService = serviceManager.getAuthService();
            UserService userService = serviceManager.getUserService();
            ExerciseService exerciseService = serviceManager.getExerciseService();
            StatisticsService statsService = serviceManager.getStatisticsService();
            
            // Test 1: Authentication Test
            System.out.println("🔐 Test 1: Authentication");
            System.out.println("Current user logged in: " + authService.isLoggedIn());
            
            // Demo kullanıcı kayıt testi (varsa skip edecek)
            boolean registerResult = authService.register("test_user", "test@example.com", "123456", "Test", "User", 2);
            System.out.println("Demo kullanıcı kaydı: " + (registerResult ? "✅ Başarılı" : "⚠️ Zaten var veya hata"));
            
            // Giriş testi
            var loginUser = authService.login("test_user", "123456");
            boolean loginResult = loginUser != null;
            System.out.println("Demo kullanıcı girişi: " + (loginResult ? "✅ Başarılı" : "❌ Başarısız"));
            
            if (authService.isLoggedIn()) {
                System.out.println("Aktif kullanıcı: " + authService.getCurrentUser().getFullName());
            }
            
            System.out.println();
            
            // Test 2: User Service Test
            System.out.println("👤 Test 2: User Service");
            if (authService.isLoggedIn()) {
                var userProfile = userService.getUserProfile();
                System.out.println("Kullanıcı profili: " + (userProfile != null ? "✅ Yüklendi" : "❌ Yüklenemedi"));
                
                if (userProfile != null) {
                    System.out.println("  - Kullanıcı: " + userProfile.getUsername());
                    System.out.println("  - Email: " + userProfile.getEmail());
                    System.out.println("  - Role: " + (userProfile.getRole() != null ? userProfile.getRole().getRoleName() : "Bilinmiyor"));
                }
            }
            
            System.out.println();
            
            // Test 3: Exercise Service Test
            System.out.println("🏋️ Test 3: Exercise Service");
            var allExercises = exerciseService.getAllExercises();
            System.out.println("Egzersiz listesi: " + allExercises.size() + " adet");
            
            var muscleGroups = exerciseService.getAllMuscleGroups();
            System.out.println("Kas grupları: " + muscleGroups.size() + " adet");
            
            var equipments = exerciseService.getAllEquipments();
            System.out.println("Ekipmanlar: " + equipments.size() + " adet");
            
            System.out.println();
            
            // Test 4: Statistics Service Test
            System.out.println("📊 Test 4: Statistics Service");
            var recentStats = statsService.getRecentBodyStats(5);
            System.out.println("Son vücut ölçümleri: " + recentStats.size() + " adet");
            
            var currentBMI = statsService.getCurrentBMI();
            System.out.println("Mevcut BMI: " + (currentBMI != null ? String.format("%.1f", currentBMI) : "Veri yok"));
            
            System.out.println();
            
            // Test 5: System Health Check
            System.out.println("🔧 Test 5: Sistem Sağlık Kontrolü");
            boolean systemHealth = serviceManager.isSystemHealthy();
            System.out.println("Sistem durumu: " + (systemHealth ? "✅ Sağlıklı" : "❌ Sorunlu"));
            
            System.out.println();
            
            // Test tamamlandı
            System.out.println("🎉 Tüm testler tamamlandı!");
            
            // Çıkış yap
            if (authService.isLoggedIn()) {
                authService.logout();
            }
            
            System.out.println("✅ Service Layer Test başarıyla tamamlandı!");
            
        } catch (Exception e) {
            System.err.println("❌ Test sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 