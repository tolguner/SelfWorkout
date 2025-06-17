package com.example.selfworkout.service;

import com.example.selfworkout.dao.BodyStatsDAO;
import com.example.selfworkout.model.BodyStats;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BodyStats için Business Logic sınıfı
 * Vücut istatistikleri yönetimi işlemlerini koordine eder
 */
public class BodyStatsService {
    
    private final BodyStatsDAO bodyStatsDAO;
    
    public BodyStatsService() {
        this.bodyStatsDAO = new BodyStatsDAO();
    }
    
    /**
     * Yeni vücut istatistiği kaydeder
     */
    public BodyStats recordBodyStats(BodyStats bodyStats) throws SQLException {
        // Validasyon
        if (bodyStats.getUserId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (bodyStats.getRecordDate() == null) {
            throw new IllegalArgumentException("Kayıt tarihi gerekli!");
        }
        
        if (bodyStats.getWeight() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kilo değeri gerekli!");
        }
        
        // Aynı tarihte kayıt var mı kontrol et (basit implementasyon)
        List<BodyStats> allUserStats = bodyStatsDAO.findByUserId(bodyStats.getUserId());
        List<BodyStats> existingStats = allUserStats.stream()
            .filter(stats -> stats.getRecordDate().equals(bodyStats.getRecordDate()))
            .collect(Collectors.toList());
        
        if (!existingStats.isEmpty()) {
            // Güncelle
            BodyStats existing = existingStats.get(0);
            existing.setWeight(bodyStats.getWeight());
            existing.setHeight(bodyStats.getHeight());
            existing.setBodyFat(bodyStats.getBodyFat());
            existing.setMuscleMass(bodyStats.getMuscleMass());
            
            bodyStatsDAO.update(existing);
            System.out.println("✅ Vücut istatistikleri güncellendi: " + existing.getRecordDate());
            return existing;
        } else {
            // Yeni kayıt
            BodyStats savedStats = bodyStatsDAO.save(bodyStats);
            System.out.println("✅ Vücut istatistikleri kaydedildi: " + savedStats.getRecordDate());
            return savedStats;
        }
    }
    
    /**
     * Vücut istatistiğini günceller
     */
    public boolean updateBodyStats(BodyStats bodyStats) throws SQLException {
        // Validasyon
        if (bodyStats.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir istatistik ID'si gerekli!");
        }
        
        if (bodyStats.getWeight() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kilo değeri gerekli!");
        }
        
        // İstatistik var mı kontrol et
        BodyStats existingStats = bodyStatsDAO.findById(bodyStats.getId());
        if (existingStats == null) {
            throw new IllegalArgumentException("Güncellenecek istatistik bulunamadı!");
        }
        
        return bodyStatsDAO.update(bodyStats);
    }
    
    /**
     * Vücut istatistiğini siler
     */
    public boolean deleteBodyStats(int statsId) throws SQLException {
        if (statsId <= 0) {
            throw new IllegalArgumentException("Geçerli bir istatistik ID'si gerekli!");
        }
        
        BodyStats stats = bodyStatsDAO.findById(statsId);
        if (stats == null) {
            throw new IllegalArgumentException("Silinecek istatistik bulunamadı!");
        }
        
        return bodyStatsDAO.delete(statsId);
    }
    
    /**
     * Kullanıcının tüm vücut istatistiklerini getirir
     */
    public List<BodyStats> getUserBodyStats(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return bodyStatsDAO.findByUserId(userId);
    }
    
    /**
     * Tarih aralığına göre vücut istatistiklerini getirir
     */
    public List<BodyStats> getBodyStatsByDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri gerekli!");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Başlangıç tarihi bitiş tarihinden sonra olamaz!");
        }
        
        // Basit implementasyon - tüm kayıtları al ve filtrele
        List<BodyStats> allStats = bodyStatsDAO.findByUserId(userId);
        return allStats.stream()
            .filter(stats -> !stats.getRecordDate().isBefore(startDate) && 
                           !stats.getRecordDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    /**
     * En son vücut istatistiğini getirir
     */
    public BodyStats getLatestBodyStats(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return bodyStatsDAO.findLatestByUserId(userId);
    }
    
    /**
     * Kilo değişimini hesaplar
     */
    public WeightProgress getWeightProgress(int userId, int days) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (days <= 0) {
            throw new IllegalArgumentException("Gün sayısı pozitif olmalı!");
        }
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        List<BodyStats> stats = getBodyStatsByDateRange(userId, startDate, endDate);
        
        if (stats.isEmpty()) {
            return new WeightProgress(0, 0, 0, startDate, endDate);
        }
        
        // İlk ve son kayıtları al
        BodyStats firstRecord = stats.get(0);
        BodyStats lastRecord = stats.get(stats.size() - 1);
        
        double startWeight = firstRecord.getWeight();
        double currentWeight = lastRecord.getWeight();
        double weightChange = currentWeight - startWeight;
        
        return new WeightProgress(startWeight, currentWeight, weightChange, startDate, endDate);
    }
    
    /**
     * BMI hesaplar
     */
    public double calculateBMI(int userId) throws SQLException {
        BodyStats latestStats = getLatestBodyStats(userId);
        
        if (latestStats == null) {
            throw new IllegalArgumentException("Kullanıcının vücut istatistiği bulunamadı!");
        }
        
        if (latestStats.getHeight() <= 0) {
            throw new IllegalArgumentException("Geçerli bir boy değeri gerekli!");
        }
        
        double heightInMeters = latestStats.getHeight() / 100.0; // cm'den m'ye
        return latestStats.getWeight() / (heightInMeters * heightInMeters);
    }
    
    /**
     * BMI kategorisini getirir
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
     * Vücut kompozisyonu analizi
     */
    public BodyComposition getBodyComposition(int userId) throws SQLException {
        BodyStats latestStats = getLatestBodyStats(userId);
        
        if (latestStats == null) {
            throw new IllegalArgumentException("Kullanıcının vücut istatistiği bulunamadı!");
        }
        
        double bmi = calculateBMI(userId);
        String bmiCategory = getBMICategory(bmi);
        
        return new BodyComposition(
            latestStats.getWeight(),
            latestStats.getHeight(),
            bmi,
            bmiCategory,
            latestStats.getBodyFat(),
            latestStats.getMuscleMass(),
            latestStats.getRecordDate()
        );
    }
    
    /**
     * Kilo değişimi için yardımcı sınıf
     */
    public static class WeightProgress {
        private final double startWeight;
        private final double currentWeight;
        private final double weightChange;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public WeightProgress(double startWeight, double currentWeight, double weightChange, 
                            LocalDate startDate, LocalDate endDate) {
            this.startWeight = startWeight;
            this.currentWeight = currentWeight;
            this.weightChange = weightChange;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public double getStartWeight() { return startWeight; }
        public double getCurrentWeight() { return currentWeight; }
        public double getWeightChange() { return weightChange; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        
        public boolean isWeightLoss() { return weightChange < 0; }
        public boolean isWeightGain() { return weightChange > 0; }
        public boolean isWeightStable() { return Math.abs(weightChange) < 0.1; }
        
        @Override
        public String toString() {
            String direction = isWeightLoss() ? "azaldı" : isWeightGain() ? "arttı" : "stabil";
            return String.format("Kilo %.1f kg'dan %.1f kg'a %s (%.1f kg %s)", 
                    startWeight, currentWeight, direction, Math.abs(weightChange), direction);
        }
    }
    
    /**
     * Vücut kompozisyonu için yardımcı sınıf
     */
    public static class BodyComposition {
        private final double weight;
        private final double height;
        private final double bmi;
        private final String bmiCategory;
        private final double bodyFatPercentage;
        private final double musclePercentage;
        private final LocalDate recordDate;
        
        public BodyComposition(double weight, double height, double bmi, String bmiCategory,
                             double bodyFatPercentage, double musclePercentage, LocalDate recordDate) {
            this.weight = weight;
            this.height = height;
            this.bmi = bmi;
            this.bmiCategory = bmiCategory;
            this.bodyFatPercentage = bodyFatPercentage;
            this.musclePercentage = musclePercentage;
            this.recordDate = recordDate;
        }
        
        public double getWeight() { return weight; }
        public double getHeight() { return height; }
        public double getBmi() { return bmi; }
        public String getBmiCategory() { return bmiCategory; }
        public double getBodyFatPercentage() { return bodyFatPercentage; }
        public double getMusclePercentage() { return musclePercentage; }
        public LocalDate getRecordDate() { return recordDate; }
        
        @Override
        public String toString() {
            return String.format("Vücut Kompozisyonu: %.1f kg, %.1f cm, BMI: %.1f (%s)", 
                    weight, height, bmi, bmiCategory);
        }
    }
} 