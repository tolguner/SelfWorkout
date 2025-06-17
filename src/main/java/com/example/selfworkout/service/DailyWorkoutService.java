package com.example.selfworkout.service;

import com.example.selfworkout.dao.DailyWorkoutDAO;
import com.example.selfworkout.dao.WorkoutExerciseDAO;
import com.example.selfworkout.model.DailyWorkout;
import com.example.selfworkout.model.WorkoutExercise;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * DailyWorkout için Business Logic sınıfı
 * Günlük antrenman yönetimi işlemlerini koordine eder
 */
public class DailyWorkoutService {
    
    private final DailyWorkoutDAO dailyWorkoutDAO;
    private final WorkoutExerciseDAO workoutExerciseDAO;
    
    public DailyWorkoutService() {
        this.dailyWorkoutDAO = new DailyWorkoutDAO();
        this.workoutExerciseDAO = new WorkoutExerciseDAO();
    }
    
    /**
     * Yeni günlük antrenman oluşturur
     */
    public DailyWorkout createDailyWorkout(DailyWorkout dailyWorkout) throws SQLException {
        // Validasyon
        if (dailyWorkout.getUserId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (dailyWorkout.getWorkoutDate() == null) {
            throw new IllegalArgumentException("Antrenman tarihi gerekli!");
        }
        
        // Günlük antrenman oluştur
        DailyWorkout savedWorkout = dailyWorkoutDAO.save(dailyWorkout);
        System.out.println("✅ Günlük antrenman başarıyla oluşturuldu: " + savedWorkout.getWorkoutDate());
        
        return savedWorkout;
    }
    
    /**
     * Günlük antrenmanı günceller
     */
    public boolean updateDailyWorkout(DailyWorkout dailyWorkout) throws SQLException {
        // Validasyon
        if (dailyWorkout.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir antrenman ID'si gerekli!");
        }
        
        // Antrenman var mı kontrol et
        DailyWorkout existingWorkout = dailyWorkoutDAO.findById(dailyWorkout.getId());
        if (existingWorkout == null) {
            throw new IllegalArgumentException("Güncellenecek antrenman bulunamadı!");
        }
        
        return dailyWorkoutDAO.update(dailyWorkout);
    }
    
    /**
     * Günlük antrenmanı siler (egzersizleri ile birlikte)
     */
    public boolean deleteDailyWorkout(int workoutId) throws SQLException {
        // Antrenman var mı kontrol et
        DailyWorkout workout = dailyWorkoutDAO.findById(workoutId);
        if (workout == null) {
            throw new IllegalArgumentException("Silinecek antrenman bulunamadı!");
        }
        
        // Önce antrenman egzersizlerini sil
        workoutExerciseDAO.deleteByWorkoutId(workoutId);
        
        // Sonra antrenmanı sil
        boolean deleted = dailyWorkoutDAO.delete(workoutId);
        
        if (deleted) {
            System.out.println("✅ Günlük antrenman ve tüm egzersizleri başarıyla silindi.");
        }
        
        return deleted;
    }
    
    /**
     * ID'ye göre günlük antrenman getirir (egzersizleri ile birlikte)
     */
    public DailyWorkout getDailyWorkoutWithExercises(int workoutId) throws SQLException {
        DailyWorkout workout = dailyWorkoutDAO.findById(workoutId);
        if (workout != null) {
            // Antrenman egzersizlerini yükle
            List<WorkoutExercise> exercises = workoutExerciseDAO.findByWorkoutId(workoutId);
            workout.setWorkoutExercises(exercises);
        }
        return workout;
    }
    
    /**
     * Kullanıcının tüm günlük antrenmanlarını getirir
     */
    public List<DailyWorkout> getUserDailyWorkouts(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return dailyWorkoutDAO.findByUserId(userId);
    }
    
    /**
     * Tarih aralığına göre günlük antrenmanları getirir
     */
    public List<DailyWorkout> getDailyWorkoutsByDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri gerekli!");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Başlangıç tarihi bitiş tarihinden sonra olamaz!");
        }
        
        return dailyWorkoutDAO.findByDateRange(userId, startDate, endDate);
    }
    
    /**
     * Bugünkü antrenmanı getirir
     */
    public DailyWorkout getTodaysWorkout(int userId) throws SQLException {
        LocalDate today = LocalDate.now();
        List<DailyWorkout> todaysWorkouts = dailyWorkoutDAO.findByDateRange(userId, today, today);
        
        if (!todaysWorkouts.isEmpty()) {
            return getDailyWorkoutWithExercises(todaysWorkouts.get(0).getId());
        }
        
        return null;
    }
    
    /**
     * Antrenman için egzersiz ekler
     */
    public WorkoutExercise addExerciseToWorkout(WorkoutExercise workoutExercise) throws SQLException {
        // Validasyon
        if (workoutExercise.getDailyWorkoutId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir antrenman ID'si gerekli!");
        }
        
        if (workoutExercise.getExerciseId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir egzersiz ID'si gerekli!");
        }
        
        // Antrenman var mı kontrol et
        DailyWorkout workout = dailyWorkoutDAO.findById(workoutExercise.getDailyWorkoutId());
        if (workout == null) {
            throw new IllegalArgumentException("Antrenman bulunamadı!");
        }
        
        return workoutExerciseDAO.save(workoutExercise);
    }
    
    /**
     * Antrenman egzersizini kaldırır
     */
    public boolean removeExerciseFromWorkout(int workoutExerciseId) throws SQLException {
        if (workoutExerciseId <= 0) {
            throw new IllegalArgumentException("Geçerli bir antrenman egzersiz ID'si gerekli!");
        }
        
        return workoutExerciseDAO.delete(workoutExerciseId);
    }
    
    /**
     * Antrenman süresini hesaplar ve günceller
     */
    public boolean calculateAndUpdateDuration(int workoutId) throws SQLException {
        DailyWorkout workout = dailyWorkoutDAO.findById(workoutId);
        if (workout == null) {
            throw new IllegalArgumentException("Antrenman bulunamadı!");
        }
        
        // Egzersizleri al
        List<WorkoutExercise> exercises = workoutExerciseDAO.findByWorkoutId(workoutId);
        
        // Toplam süreyi hesapla (basit hesaplama: set sayısı * 3 dakika)
        int totalDuration = 0;
        for (WorkoutExercise exercise : exercises) {
            totalDuration += exercise.getSetCount() * 3; // Her set için 3 dakika
        }
        
        // Süreyi güncelle
        workout.setTotalDuration(totalDuration);
        return dailyWorkoutDAO.update(workout);
    }
    
    /**
     * Haftalık antrenman istatistiklerini getirir
     */
    public WeeklyWorkoutStats getWeeklyStats(int userId) throws SQLException {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Son 7 gün
        
        List<DailyWorkout> weeklyWorkouts = dailyWorkoutDAO.findByDateRange(userId, startDate, endDate);
        
        int totalWorkouts = weeklyWorkouts.size();
        int totalDuration = weeklyWorkouts.stream()
                .mapToInt(DailyWorkout::getTotalDuration)
                .sum();
        
        return new WeeklyWorkoutStats(totalWorkouts, totalDuration, startDate, endDate);
    }
    
    /**
     * Haftalık antrenman istatistikleri için yardımcı sınıf
     */
    public static class WeeklyWorkoutStats {
        private final int totalWorkouts;
        private final int totalDuration;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public WeeklyWorkoutStats(int totalWorkouts, int totalDuration, LocalDate startDate, LocalDate endDate) {
            this.totalWorkouts = totalWorkouts;
            this.totalDuration = totalDuration;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public int getTotalWorkouts() { return totalWorkouts; }
        public int getTotalDuration() { return totalDuration; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public double getAverageDuration() { 
            return totalWorkouts > 0 ? (double) totalDuration / totalWorkouts : 0; 
        }
        
        @Override
        public String toString() {
            return String.format("Haftalık İstatistik: %d antrenman, %d dakika toplam süre", 
                    totalWorkouts, totalDuration);
        }
    }
} 