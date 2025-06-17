package com.example.selfworkout.service;

import com.example.selfworkout.dao.UserWorkoutDAO;
import com.example.selfworkout.dao.ExerciseDAO;
import com.example.selfworkout.model.UserWorkout;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.Workout;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

/**
 * Workout yönetimi için Service sınıfı
 */
public class WorkoutService {
    
    private final UserWorkoutDAO userWorkoutDAO;
    private final ExerciseDAO exerciseDAO;
    private final AuthenticationService authService;
    
    public WorkoutService(AuthenticationService authService) {
        this.userWorkoutDAO = new UserWorkoutDAO();
        this.exerciseDAO = new ExerciseDAO();
        this.authService = authService;
    }
    
    /**
     * Yeni antrenman başlatır
     */
    public UserWorkout startWorkout(int exerciseId, String notes) {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            // Egzersizi kontrol et
            Exercise exercise = exerciseDAO.findById(exerciseId);
            if (exercise == null) {
                System.out.println("❌ Egzersiz bulunamadı!");
                return null;
            }
            
            // Aktif antrenman var mı kontrol et
            List<UserWorkout> activeWorkouts = userWorkoutDAO.findActiveWorkouts(authService.getCurrentUserId());
            if (!activeWorkouts.isEmpty()) {
                System.out.println("❌ Zaten aktif bir antrenmanınız var! Önce onu tamamlayın.");
                return null;
            }
            
            // Yeni antrenman oluştur
            UserWorkout workout = new UserWorkout();
            workout.setUserId(authService.getCurrentUserId().intValue());
            workout.setExerciseId(exerciseId);
            workout.setStatus("active");
            workout.setNotes(notes != null ? notes.trim() : "");
            workout.setStartedAt(LocalDateTime.now());
            
            UserWorkout savedWorkout = userWorkoutDAO.save(workout);
            
            if (savedWorkout != null) {
                System.out.println("🏋️ Antrenman başlatıldı: " + exercise.getName());
                return savedWorkout;
            } else {
                System.out.println("❌ Antrenman başlatılamadı!");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Antrenman başlatma hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Antrenmanı tamamlar
     */
    public boolean completeWorkout(int workoutId, int sets, int reps, double weight, String notes) {
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            UserWorkout workout = userWorkoutDAO.findById(workoutId);
            if (workout == null) {
                System.out.println("❌ Antrenman bulunamadı!");
                return false;
            }
            
            // Yetki kontrolü
            if (workout.getUserId() != authService.getCurrentUserId()) {
                System.out.println("❌ Bu antrenmanı tamamlama yetkiniz yok!");
                return false;
            }
            
            // Status kontrolü
            if (!"active".equals(workout.getStatus())) {
                System.out.println("❌ Bu antrenman zaten tamamlanmış!");
                return false;
            }
            
            // Validation
            if (sets <= 0 || reps <= 0 || weight < 0) {
                System.out.println("❌ Geçersiz antrenman verileri!");
                return false;
            }
            
            // Süre hesapla
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(workout.getStartedAt(), now);
            int durationMinutes = (int) duration.toMinutes();
            
            // Güncelle
            workout.setStatus("completed");
            workout.setSets(sets);
            workout.setReps(reps);
            workout.setWeight(weight);
            workout.setNotes(notes != null ? notes.trim() : workout.getNotes());
            workout.setCompletedAt(now);
            workout.setDurationMinutes(durationMinutes);
            
            boolean success = userWorkoutDAO.update(workout);
            
            if (success) {
                System.out.println("✅ Antrenman tamamlandı! Süre: " + durationMinutes + " dk");
                return true;
            } else {
                System.out.println("❌ Antrenman tamamlanamadı!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Antrenman tamamlama hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Antrenmanı iptal eder
     */
    public boolean cancelWorkout(int workoutId, String reason) {
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            UserWorkout workout = userWorkoutDAO.findById(workoutId);
            if (workout == null) {
                System.out.println("❌ Antrenman bulunamadı!");
                return false;
            }
            
            // Yetki kontrolü
            if (workout.getUserId() != authService.getCurrentUserId()) {
                System.out.println("❌ Bu antrenmanı iptal etme yetkiniz yok!");
                return false;
            }
            
            // Status kontrolü
            if (!"active".equals(workout.getStatus())) {
                System.out.println("❌ Bu antrenman zaten tamamlanmış!");
                return false;
            }
            
            // İptal et
            workout.setStatus("cancelled");
            workout.setNotes((workout.getNotes() != null ? workout.getNotes() + " | " : "") + 
                           "İptal edildi: " + (reason != null ? reason : ""));
            
            boolean success = userWorkoutDAO.update(workout);
            
            if (success) {
                System.out.println("🚫 Antrenman iptal edildi.");
                return true;
            } else {
                System.out.println("❌ Antrenman iptal edilemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Antrenman iptal etme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kullanıcının aktif antrenmanını getirir
     */
    public UserWorkout getActiveWorkout() {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            List<UserWorkout> activeWorkouts = userWorkoutDAO.findActiveWorkouts(authService.getCurrentUserId());
            return activeWorkouts.isEmpty() ? null : activeWorkouts.get(0);
            
        } catch (SQLException e) {
            System.err.println("❌ Aktif antrenman yüklenirken hata: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Belirli kullanıcının aktif antrenmanını getirir
     */
    public Workout getActiveWorkout(int userId) {
        try {
            List<UserWorkout> activeWorkouts = userWorkoutDAO.findActiveWorkouts(userId);
            if (activeWorkouts.isEmpty()) {
                return null;
            }
            
            // UserWorkout'u Workout'a dönüştür
            UserWorkout userWorkout = activeWorkouts.get(0);
            Workout workout = new Workout();
            workout.setId(userWorkout.getId());
            workout.setUserId(userWorkout.getUserId());
            workout.setExerciseId(userWorkout.getExerciseId());
            workout.setExerciseName(userWorkout.getWorkoutTitle()); // Egzersiz adını workout title'dan al
            workout.setPlannedSets(userWorkout.getSets());
            workout.setPlannedReps(userWorkout.getReps());
            workout.setWeight(userWorkout.getWeight());
            workout.setNotes(userWorkout.getNotes());
            workout.setStartTime(userWorkout.getWorkoutDate().atStartOfDay()); // LocalDate'i LocalDateTime'a çevir
            workout.setStatus("ACTIVE");
            
            return workout;
            
        } catch (SQLException e) {
            System.err.println("❌ Aktif antrenman yüklenirken hata: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Antrenmanı iptal eder (sadece ID ile)
     */
    public void cancelWorkout(int workoutId) throws Exception {
        cancelWorkout(workoutId, "Kullanıcı tarafından iptal edildi");
    }
    
    /**
     * Workout oluşturur
     */
    public void createWorkout(Workout workout) throws Exception {
        if (workout == null) {
            throw new Exception("Workout bilgisi boş olamaz");
        }
        
        try {
            // Workout'u UserWorkout'a dönüştür
            UserWorkout userWorkout = new UserWorkout();
            userWorkout.setUserId(workout.getUserId());
            userWorkout.setExerciseId(workout.getExerciseId());
            userWorkout.setWorkoutType("daily"); // Tek egzersiz antrenmanı
            userWorkout.setSets(workout.getPlannedSets());
            userWorkout.setReps(workout.getPlannedReps());
            userWorkout.setWeight(workout.getWeight());
            
            // Notes'a egzersiz adını ve kullanıcı notlarını ekle
            String combinedNotes = "Egzersiz: " + workout.getExerciseName();
            if (workout.getNotes() != null && !workout.getNotes().trim().isEmpty()) {
                combinedNotes += " | " + workout.getNotes();
            }
            userWorkout.setNotes(combinedNotes);
            
            userWorkout.setWorkoutDate(workout.getStartTime().toLocalDate());
            userWorkout.setStartedAt(workout.getStartTime());
            userWorkout.setStatus("active");
            userWorkout.setDurationMinutes(0); // Başlangıçta 0
            
            UserWorkout savedWorkout = userWorkoutDAO.save(userWorkout);
            if (savedWorkout == null) {
                throw new Exception("Antrenman kaydedilemedi");
            }
            
        } catch (SQLException e) {
            throw new Exception("Antrenman oluşturulurken hata: " + e.getMessage());
        }
    }
    
    /**
     * Kullanıcının antrenman geçmişini getirir
     */
    public List<UserWorkout> getWorkoutHistory(int limit) {
        if (!authService.requireLogin()) {
            return new ArrayList<>();
        }
        
        try {
            return userWorkoutDAO.findByUserId(authService.getCurrentUserId(), limit);
        } catch (SQLException e) {
            System.err.println("❌ Antrenman geçmişi yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Belirli bir egzersiz için antrenman geçmişini getirir
     */
    public List<UserWorkout> getExerciseHistory(int exerciseId, int limit) {
        if (!authService.requireLogin()) {
            return new ArrayList<>();
        }
        
        try {
            return userWorkoutDAO.findByUserAndExercise(authService.getCurrentUserId(), exerciseId, limit);
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz geçmişi yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Tüm antrenmanları getirir (admin için)
     */
    public List<UserWorkout> getAllWorkouts() {
        if (!authService.requireAdmin()) {
            return new ArrayList<>();
        }
        
        try {
            return userWorkoutDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Antrenmanlar yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Antrenman istatistiklerini getirir
     */
    public WorkoutStats getWorkoutStats() {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            List<UserWorkout> allWorkouts = userWorkoutDAO.findByUserId(authService.getCurrentUserId(), 1000);
            
            WorkoutStats stats = new WorkoutStats();
            int totalWorkouts = 0;
            int completedWorkouts = 0;
            int cancelledWorkouts = 0;
            int totalDuration = 0;
            double totalWeight = 0;
            int totalSets = 0;
            int totalReps = 0;
            
            for (UserWorkout workout : allWorkouts) {
                totalWorkouts++;
                
                if ("completed".equals(workout.getStatus())) {
                    completedWorkouts++;
                    totalDuration += workout.getDurationMinutes();
                    totalWeight += workout.getWeight();
                    totalSets += workout.getSets();
                    totalReps += workout.getReps();
                } else if ("cancelled".equals(workout.getStatus())) {
                    cancelledWorkouts++;
                }
            }
            
            stats.totalWorkouts = totalWorkouts;
            stats.completedWorkouts = completedWorkouts;
            stats.cancelledWorkouts = cancelledWorkouts;
            stats.averageDuration = completedWorkouts > 0 ? totalDuration / completedWorkouts : 0;
            stats.totalWeight = totalWeight;
            stats.totalSets = totalSets;
            stats.totalReps = totalReps;
            stats.completionRate = totalWorkouts > 0 ? (double) completedWorkouts / totalWorkouts * 100 : 0;
            
            return stats;
            
        } catch (SQLException e) {
            System.err.println("❌ İstatistik hesaplama hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Antrenman istatistikleri için veri sınıfı
     */
    public static class WorkoutStats {
        public int totalWorkouts;
        public int completedWorkouts;
        public int cancelledWorkouts;
        public int averageDuration; // dakika
        public double totalWeight;
        public int totalSets;
        public int totalReps;
        public double completionRate; // yüzde
        
        @Override
        public String toString() {
            return String.format(
                "📊 Antrenman İstatistikleri:\n" +
                "- Toplam Antrenman: %d\n" +
                "- Tamamlanan: %d\n" +
                "- İptal Edilen: %d\n" +
                "- Ortalama Süre: %d dk\n" +
                "- Toplam Ağırlık: %.1f kg\n" +
                "- Toplam Set: %d\n" +
                "- Toplam Tekrar: %d\n" +
                "- Tamamlama Oranı: %.1f%%",
                totalWorkouts, completedWorkouts, cancelledWorkouts,
                averageDuration, totalWeight, totalSets, totalReps, completionRate
            );
        }
    }
} 