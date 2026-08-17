package com.example.selfworkout.service;

import com.example.selfworkout.dao.BodyStatsDAO;
import com.example.selfworkout.dao.ReportingDAO;
import com.example.selfworkout.model.BodyStats;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * İstatistik ve body stats yönetimi için Service sınıfı
 */
public class StatisticsService {
    
    private final BodyStatsDAO bodyStatsDAO;
    private final ReportingDAO reportingDAO;
    private final AuthenticationService authService;

    public StatisticsService(AuthenticationService authService) {
        this.bodyStatsDAO = new BodyStatsDAO();
        this.reportingDAO = new ReportingDAO();
        this.authService = authService;
    }

    /**
     * Giriş yapmış kullanıcının pano istatistiklerini tek sorguda döndürür.
     * sp_UserDashboardStats yordamını kullanır.
     */
    public ReportingDAO.DashboardStats getDashboardStats() {
        if (!authService.requireLogin()) {
            return null;
        }

        try {
            return reportingDAO.findDashboardStats(authService.getCurrentUserId());
        } catch (SQLException e) {
            System.err.println("❌ Pano istatistikleri alınamadı: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kullanıcının güncel kesintisiz antrenman serisi (gün).
     * Hesaplama fn_WorkoutStreak fonksiyonunda yapılır.
     */
    public int getCurrentWorkoutStreak() {
        if (!authService.requireLogin()) {
            return 0;
        }

        try {
            return reportingDAO.findCurrentStreak(authService.getCurrentUserId());
        } catch (SQLException e) {
            System.err.println("❌ Antrenman serisi hesaplanamadı: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Kullanıcının şimdiye kadarki en uzun antrenman serisi (gün).
     */
    public int getLongestWorkoutStreak() {
        if (!authService.requireLogin()) {
            return 0;
        }

        try {
            return reportingDAO.findLongestStreak(authService.getCurrentUserId());
        } catch (SQLException e) {
            System.err.println("❌ En uzun antrenman serisi hesaplanamadı: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Kullanıcının bir egzersizdeki tahmini tek tekrar maksimumu (Epley).
     */
    public double getEstimatedOneRepMax(int exerciseId) {
        if (!authService.requireLogin()) {
            return 0.0;
        }

        try {
            return reportingDAO.findEstimatedOneRepMax(authService.getCurrentUserId(), exerciseId);
        } catch (SQLException e) {
            System.err.println("❌ Tahmini 1RM hesaplanamadı: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Yeni vücut ölçümü ekler
     */
    public boolean addBodyStats(double weight, double height, double bodyFat, double muscleMass, String notes) {
        if (!authService.requireLogin()) {
            return false;
        }
        
        // Validasyonlar
        if (!isValidWeight(weight) || !isValidHeight(height)) {
            return false;
        }
        
        try {
            BodyStats bodyStats = new BodyStats();
            bodyStats.setUserId(authService.getCurrentUserId());
            bodyStats.setWeight(weight);
            bodyStats.setHeight(height);
            bodyStats.setBodyFat(bodyFat);
            bodyStats.setMuscleMass(muscleMass);
            bodyStats.setNotes(notes != null ? notes.trim() : "");
            bodyStats.setRecordDate(LocalDate.now());
            
            BodyStats saved = bodyStatsDAO.save(bodyStats);
            
            if (saved != null) {
                double bmi = calculateBMI(weight, height);
                System.out.println("✅ Vücut ölçümü kaydedildi! BMI: " + String.format("%.1f", bmi));
                return true;
            } else {
                System.out.println("❌ Vücut ölçümü kaydedilemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Vücut ölçümü kaydetme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kullanıcının son vücut ölçümlerini getirir
     */
    public List<BodyStats> getRecentBodyStats(int limit) {
        if (!authService.requireLogin()) {
            return new ArrayList<>();
        }
        
        try {
            return bodyStatsDAO.findByUserId(authService.getCurrentUserId(), limit);
        } catch (SQLException e) {
            System.err.println("❌ Vücut ölçümleri yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Belirli tarih aralığındaki ölçümleri getirir
     */
    public List<BodyStats> getBodyStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (!authService.requireLogin()) {
            return new ArrayList<>();
        }
        
        try {
            return bodyStatsDAO.findByUserAndDateRange(authService.getCurrentUserId(), startDate, endDate);
        } catch (SQLException e) {
            System.err.println("❌ Tarih aralığı ölçümleri yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Kullanıcının en son BMI'sını hesaplar
     */
    public Double getCurrentBMI() {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            BodyStats latest = bodyStatsDAO.findLatestByUserId(authService.getCurrentUserId());
            if (latest != null) {
                return calculateBMI(latest.getWeight(), latest.getHeight());
            }
            return null;
        } catch (SQLException e) {
            System.err.println("❌ BMI hesaplama hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * BMI hesaplama metodu
     */
    public double calculateBMI(double weight, double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Boy değeri pozitif olmalıdır!");
        }
        
        double heightInMeters = height / 100.0; // cm'den metre'ye çevir
        return weight / (heightInMeters * heightInMeters);
    }
    
    /**
     * BMI kategorisini döndürür
     */
    public String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Zayıf";
        } else if (bmi < 25) {
            return "Normal";
        } else if (bmi < 30) {
            return "Fazla Kilolu";
        } else {
            return "Obez";
        }
    }
    
    /**
     * Kilo değişim analizini getirir
     */
    public WeightChangeAnalysis getWeightChangeAnalysis(int days) {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            List<BodyStats> stats = bodyStatsDAO.findByUserAndDateRange(
                authService.getCurrentUserId(), startDate, endDate);
            
            if (stats.size() < 2) {
                return new WeightChangeAnalysis("Analiz için yeterli veri yok", 0, 0, 0);
            }
            
            // En eski ve en yeni ölçümleri al
            BodyStats oldest = stats.get(stats.size() - 1);
            BodyStats newest = stats.get(0);
            
            double weightChange = newest.getWeight() - oldest.getWeight();
            double totalDays = oldest.getRecordDate().until(newest.getRecordDate()).getDays();
            double weeklyChange = totalDays > 0 ? (weightChange / totalDays) * 7 : 0;
            
            String trend;
            if (weightChange > 0.5) {
                trend = "Kilo Artışı";
            } else if (weightChange < -0.5) {
                trend = "Kilo Kaybı";
            } else {
                trend = "Stabil";
            }
            
            return new WeightChangeAnalysis(trend, weightChange, weeklyChange, totalDays);
            
        } catch (SQLException e) {
            System.err.println("❌ Kilo analizi hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Detaylı vücut istatistiklerini getirir
     */
    public BodyStatsReport getBodyStatsReport() {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            BodyStats latest = bodyStatsDAO.findLatestByUserId(authService.getCurrentUserId());
            if (latest == null) {
                return new BodyStatsReport("Henüz vücut ölçümü kaydedilmemiş");
            }
            
            double bmi = calculateBMI(latest.getWeight(), latest.getHeight());
            String bmiCategory = getBMICategory(bmi);
            
            WeightChangeAnalysis monthlyChange = getWeightChangeAnalysis(30);
            
            return new BodyStatsReport(latest, bmi, bmiCategory, monthlyChange);
            
        } catch (SQLException e) {
            System.err.println("❌ Vücut raporu oluşturma hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Ağırlık validasyonu
     */
    private boolean isValidWeight(double weight) {
        if (weight <= 0 || weight > 500) {
            System.out.println("❌ Geçerli bir ağırlık giriniz (0-500 kg)!");
            return false;
        }
        return true;
    }
    
    /**
     * Boy validasyonu
     */
    private boolean isValidHeight(double height) {
        if (height <= 0 || height > 250) {
            System.out.println("❌ Geçerli bir boy giriniz (0-250 cm)!");
            return false;
        }
        return true;
    }
    
    /**
     * Kilo değişim analizi için veri sınıfı
     */
    public static class WeightChangeAnalysis {
        public final String trend;
        public final double totalChange;
        public final double weeklyChange;
        public final double days;
        
        public WeightChangeAnalysis(String trend, double totalChange, double weeklyChange, double days) {
            this.trend = trend;
            this.totalChange = totalChange;
            this.weeklyChange = weeklyChange;
            this.days = days;
        }
        
        @Override
        public String toString() {
            return String.format(
                "📈 Kilo Değişim Analizi (%d gün):\n" +
                "- Genel Trend: %s\n" +
                "- Toplam Değişim: %.1f kg\n" +
                "- Haftalık Ortalama: %.1f kg",
                (int) days, trend, totalChange, weeklyChange
            );
        }
    }
    
    /**
     * Vücut istatistikleri raporu için veri sınıfı
     */
    public static class BodyStatsReport {
        public final BodyStats currentStats;
        public final double bmi;
        public final String bmiCategory;
        public final WeightChangeAnalysis monthlyChange;
        public final String message;
        
        public BodyStatsReport(String message) {
            this.message = message;
            this.currentStats = null;
            this.bmi = 0;
            this.bmiCategory = "";
            this.monthlyChange = null;
        }
        
        public BodyStatsReport(BodyStats currentStats, double bmi, String bmiCategory, 
                             WeightChangeAnalysis monthlyChange) {
            this.currentStats = currentStats;
            this.bmi = bmi;
            this.bmiCategory = bmiCategory;
            this.monthlyChange = monthlyChange;
            this.message = null;
        }
        
        @Override
        public String toString() {
            if (message != null) {
                return message;
            }
            
            StringBuilder report = new StringBuilder();
            report.append("📊 Vücut İstatistikleri Raporu\n");
            report.append("═══════════════════════════════\n");
            report.append(String.format("📅 Tarih: %s\n", currentStats.getRecordDate()));
            report.append(String.format("⚖️ Ağırlık: %.1f kg\n", currentStats.getWeight()));
            report.append(String.format("📏 Boy: %.1f cm\n", currentStats.getHeight()));
            report.append(String.format("📐 BMI: %.1f (%s)\n", bmi, bmiCategory));
            
            if (currentStats.getBodyFat() > 0) {
                report.append(String.format("🔥 Vücut Yağ Oranı: %.1f%%\n", currentStats.getBodyFat()));
            }
            
            if (currentStats.getMuscleMass() > 0) {
                report.append(String.format("💪 Kas Kütlesi: %.1f kg\n", currentStats.getMuscleMass()));
            }
            
            if (monthlyChange != null) {
                report.append("\n").append(monthlyChange.toString());
            }
            
            return report.toString();
        }
    }
} 