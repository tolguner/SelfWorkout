package com.example.selfworkout.service;

/**
 * Tüm servisleri merkezi olarak yöneten Singleton sınıfı
 */
public class ServiceManager {
    
    private static ServiceManager instance;
    
    // Servisler
    private final AuthenticationService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final StatisticsService statisticsService;
    private final MuscleGroupService muscleGroupService;
    private final EquipmentService equipmentService;
    private final LogService logService;
    private final BodyStatsService bodyStatsService;
    
    /**
     * Private constructor - Singleton pattern
     */
    private ServiceManager() {
        // Authentication service'i önce oluştur (diğerleri buna bağımlı)
        this.authService = new AuthenticationService();
        
        // Diğer servisleri AuthenticationService ile başlat
        this.userService = new UserService(authService);
        this.roleService = new RoleService();
        this.exerciseService = new ExerciseService(authService);
        this.workoutService = new WorkoutService(authService);
        this.statisticsService = new StatisticsService(authService);
        this.muscleGroupService = new MuscleGroupService();
        this.equipmentService = new EquipmentService();
        this.logService = new LogService();
        this.bodyStatsService = new BodyStatsService();
        
        System.out.println("🔧 ServiceManager başlatıldı - Tüm servisler hazır!");
    }
    
    /**
     * Singleton instance'ını döndürür
     */
    public static ServiceManager getInstance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) {
                    instance = new ServiceManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * AuthenticationService'i döndürür
     */
    public AuthenticationService getAuthService() {
        return authService;
    }
    
    /**
     * AuthenticationService'i döndürür (alternatif isim)
     */
    public AuthenticationService getAuthenticationService() {
        return authService;
    }
    
    /**
     * UserService'i döndürür
     */
    public UserService getUserService() {
        return userService;
    }
    
    /**
     * RoleService'i döndürür
     */
    public RoleService getRoleService() {
        return roleService;
    }
    
    /**
     * ExerciseService'i döndürür
     */
    public ExerciseService getExerciseService() {
        return exerciseService;
    }
    
    /**
     * WorkoutService'i döndürür
     */
    public WorkoutService getWorkoutService() {
        return workoutService;
    }
    
    /**
     * StatisticsService'i döndürür
     */
    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    /**
     * MuscleGroupService'i döndürür
     */
    public MuscleGroupService getMuscleGroupService() {
        return muscleGroupService;
    }

    /**
     * EquipmentService'i döndürür
     */
    public EquipmentService getEquipmentService() {
        return equipmentService;
    }
    
    /**
     * LogService'i döndürür
     */
    public LogService getLogService() {
        return logService;
    }
    
    /**
     * BodyStatsService'i döndürür
     */
    public BodyStatsService getBodyStatsService() {
        return bodyStatsService;
    }
    
    /**
     * Sistem durumunu kontrol eder
     */
    public boolean isSystemHealthy() {
        try {
            // Database bağlantısını test et
            com.example.selfworkout.util.DatabaseConnection.testConnection();
            
            // Temel servislerin varlığını kontrol et
            return authService != null && 
                   userService != null && 
                   exerciseService != null && 
                   workoutService != null && 
                   statisticsService != null &&
                   bodyStatsService != null;
            
        } catch (Exception e) {
            System.err.println("❌ Sistem sağlık kontrolü başarısız: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sistem bilgilerini yazdırır
     */
    public void printSystemInfo() {
        System.out.println("\n🏋️ SelfWorkout Fitness Tracking System");
        System.out.println("=====================================");
        System.out.println("📊 Mevcut Servisler:");
        System.out.println("  ✅ AuthenticationService - Giriş/çıkış yönetimi");
        System.out.println("  ✅ UserService - Kullanıcı yönetimi");
        System.out.println("  ✅ ExerciseService - Egzersiz yönetimi");
        System.out.println("  ✅ WorkoutService - Antrenman takibi");
        System.out.println("  ✅ StatisticsService - İstatistik hesaplamaları");
        System.out.println("  ✅ BodyStatsService - Vücut ölçümü takibi");
        System.out.println("🔧 Sistem Durumu: " + (isSystemHealthy() ? "✅ Sağlıklı" : "❌ Sorunlu"));
        System.out.println("=====================================\n");
    }
    
    /**
     * Cleanup metodu - Uygulama kapanırken çağrılabilir
     */
    public void shutdown() {
        System.out.println("🔌 ServiceManager kapatılıyor...");
        
        // Aktif kullanıcıyı çıkart
        if (authService.isLoggedIn()) {
            authService.logout();
        }
        
        // Instance'ı temizle
        instance = null;
        
        System.out.println("✅ ServiceManager başarıyla kapatıldı!");
    }
} 