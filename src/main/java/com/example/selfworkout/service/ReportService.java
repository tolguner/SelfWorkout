package com.example.selfworkout.service;

import com.example.selfworkout.dao.*;
import com.example.selfworkout.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Report için Business Logic sınıfı
 * Çeşitli raporlar ve analizler oluşturur
 */
public class ReportService {
    
    private final UserDAO userDAO;
    private final ExerciseDAO exerciseDAO;
    private final DailyWorkoutDAO dailyWorkoutDAO;
    private final BodyStatsDAO bodyStatsDAO;
    private final LogDAO logDAO;
    
    public ReportService() {
        this.userDAO = new UserDAO();
        this.exerciseDAO = new ExerciseDAO();
        this.dailyWorkoutDAO = new DailyWorkoutDAO();
        this.bodyStatsDAO = new BodyStatsDAO();
        this.logDAO = new LogDAO();
    }
    
    /**
     * Kullanıcı aktivite raporu oluşturur
     */
    public UserActivityReport generateUserActivityReport(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        // Validasyon
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri gerekli!");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Başlangıç tarihi bitiş tarihinden sonra olamaz!");
        }
        
        // Kullanıcı bilgisi
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı!");
        }
        
        // Antrenman verileri
        List<DailyWorkout> workouts = dailyWorkoutDAO.findByUserId(userId).stream()
            .filter(w -> !w.getWorkoutDate().isBefore(startDate) && !w.getWorkoutDate().isAfter(endDate))
            .collect(Collectors.toList());
        
        // Vücut istatistikleri
        List<BodyStats> bodyStats = bodyStatsDAO.findByUserId(userId).stream()
            .filter(bs -> !bs.getRecordDate().isBefore(startDate) && !bs.getRecordDate().isAfter(endDate))
            .collect(Collectors.toList());
        
        // İstatistikleri hesapla
        int totalWorkouts = workouts.size();
        int totalDuration = workouts.stream().mapToInt(DailyWorkout::getTotalDuration).sum();
        double averageDuration = totalWorkouts > 0 ? (double) totalDuration / totalWorkouts : 0;
        
        // Kilo değişimi
        double weightChange = 0;
        if (bodyStats.size() >= 2) {
            BodyStats firstStats = bodyStats.get(0);
            BodyStats lastStats = bodyStats.get(bodyStats.size() - 1);
            weightChange = lastStats.getWeight() - firstStats.getWeight();
        }
        
        return new UserActivityReport(user, startDate, endDate, totalWorkouts, 
                totalDuration, averageDuration, weightChange, workouts, bodyStats);
    }
    
    /**
     * Sistem genel raporu oluşturur
     */
    public SystemOverviewReport generateSystemOverviewReport() throws SQLException {
        // Kullanıcı istatistikleri
        List<User> allUsers = userDAO.findAll();
        int totalUsers = allUsers.size();
        
        // Egzersiz istatistikleri
        List<Exercise> allExercises = exerciseDAO.findAll();
        int totalExercises = allExercises.size();
        
        // Antrenman istatistikleri - tüm kullanıcıların antrenmanlarını topla
        int totalWorkouts = 0;
        for (User user : allUsers) {
            List<DailyWorkout> userWorkouts = dailyWorkoutDAO.findByUserId(user.getId());
            totalWorkouts += userWorkouts.size();
        }
        
        // Log istatistikleri
        List<Log> allLogs = logDAO.findAll();
        int totalLogs = allLogs.size();
        
        // Son 30 günde kayıt olan kullanıcılar
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int newUsersLast30Days = (int) allUsers.stream()
            .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(thirtyDaysAgo))
            .count();
        
        // Son 7 günde yapılan antrenmanlar
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        int workoutsLast7Days = 0;
        for (User user : allUsers) {
            List<DailyWorkout> userWorkouts = dailyWorkoutDAO.findByDateRange(user.getId(), sevenDaysAgo, LocalDate.now());
            workoutsLast7Days += userWorkouts.size();
        }
        
        return new SystemOverviewReport(totalUsers, totalExercises, totalWorkouts, 
                totalLogs, newUsersLast30Days, workoutsLast7Days);
    }
    
    /**
     * Popüler egzersizler raporu oluşturur
     */
    public PopularExercisesReport generatePopularExercisesReport(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit pozitif olmalı!");
        }
        
        List<Exercise> allExercises = exerciseDAO.findAll();
        
        // Basit popülerlik hesaplama (gerçek uygulamada kullanım sayısı kullanılabilir)
        List<ExercisePopularity> popularExercises = allExercises.stream()
            .map(exercise -> {
                // Basit popülerlik skoru (alfabetik sıra + rastgele faktör)
                int popularityScore = 100 - exercise.getName().length() + (exercise.getId() % 10);
                return new ExercisePopularity(exercise, popularityScore);
            })
            .sorted((e1, e2) -> Integer.compare(e2.getPopularityScore(), e1.getPopularityScore()))
            .limit(limit)
            .collect(Collectors.toList());
        
        return new PopularExercisesReport(popularExercises);
    }
    
    /**
     * Haftalık özet raporu oluşturur
     */
    public WeeklySummaryReport generateWeeklySummaryReport() throws SQLException {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Son 7 gün
        
        // Tüm kullanıcıların haftalık aktivitesi
        List<User> allUsers = userDAO.findAll();
        List<DailyWorkout> weeklyWorkouts = new ArrayList<>();
        
        // Her kullanıcının haftalık antrenmanlarını topla
        for (User user : allUsers) {
            List<DailyWorkout> userWorkouts = dailyWorkoutDAO.findByDateRange(user.getId(), startDate, endDate);
            weeklyWorkouts.addAll(userWorkouts);
        }
        
        // Aktif kullanıcı sayısı (bu hafta antrenman yapanlar)
        int activeUsers = (int) weeklyWorkouts.stream()
            .map(DailyWorkout::getUserId)
            .distinct()
            .count();
        
        // Toplam antrenman süresi
        int totalDuration = weeklyWorkouts.stream()
            .mapToInt(DailyWorkout::getTotalDuration)
            .sum();
        
        // Günlük ortalama antrenman sayısı
        double averageWorkoutsPerDay = weeklyWorkouts.size() / 7.0;
        
        return new WeeklySummaryReport(startDate, endDate, activeUsers, 
                weeklyWorkouts.size(), totalDuration, averageWorkoutsPerDay);
    }
    
    /**
     * Kullanıcı aktivite raporu için yardımcı sınıf
     */
    public static class UserActivityReport {
        private final User user;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int totalWorkouts;
        private final int totalDuration;
        private final double averageDuration;
        private final double weightChange;
        private final List<DailyWorkout> workouts;
        private final List<BodyStats> bodyStats;
        
        public UserActivityReport(User user, LocalDate startDate, LocalDate endDate,
                                int totalWorkouts, int totalDuration, double averageDuration,
                                double weightChange, List<DailyWorkout> workouts, List<BodyStats> bodyStats) {
            this.user = user;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalWorkouts = totalWorkouts;
            this.totalDuration = totalDuration;
            this.averageDuration = averageDuration;
            this.weightChange = weightChange;
            this.workouts = workouts;
            this.bodyStats = bodyStats;
        }
        
        // Getters
        public User getUser() { return user; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTotalWorkouts() { return totalWorkouts; }
        public int getTotalDuration() { return totalDuration; }
        public double getAverageDuration() { return averageDuration; }
        public double getWeightChange() { return weightChange; }
        public List<DailyWorkout> getWorkouts() { return workouts; }
        public List<BodyStats> getBodyStats() { return bodyStats; }
        
        @Override
        public String toString() {
            return String.format("Kullanıcı: %s, Antrenman: %d, Süre: %d dk, Kilo Değişimi: %.1f kg",
                    user.getUsername(), totalWorkouts, totalDuration, weightChange);
        }
    }
    
    /**
     * Sistem genel raporu için yardımcı sınıf
     */
    public static class SystemOverviewReport {
        private final int totalUsers;
        private final int totalExercises;
        private final int totalWorkouts;
        private final int totalLogs;
        private final int newUsersLast30Days;
        private final int workoutsLast7Days;
        
        public SystemOverviewReport(int totalUsers, int totalExercises, int totalWorkouts,
                                  int totalLogs, int newUsersLast30Days, int workoutsLast7Days) {
            this.totalUsers = totalUsers;
            this.totalExercises = totalExercises;
            this.totalWorkouts = totalWorkouts;
            this.totalLogs = totalLogs;
            this.newUsersLast30Days = newUsersLast30Days;
            this.workoutsLast7Days = workoutsLast7Days;
        }
        
        // Getters
        public int getTotalUsers() { return totalUsers; }
        public int getTotalExercises() { return totalExercises; }
        public int getTotalWorkouts() { return totalWorkouts; }
        public int getTotalLogs() { return totalLogs; }
        public int getNewUsersLast30Days() { return newUsersLast30Days; }
        public int getWorkoutsLast7Days() { return workoutsLast7Days; }
        
        @Override
        public String toString() {
            return String.format("Sistem Özeti: %d kullanıcı, %d egzersiz, %d antrenman",
                    totalUsers, totalExercises, totalWorkouts);
        }
    }
    
    /**
     * Popüler egzersizler raporu için yardımcı sınıf
     */
    public static class PopularExercisesReport {
        private final List<ExercisePopularity> popularExercises;
        
        public PopularExercisesReport(List<ExercisePopularity> popularExercises) {
            this.popularExercises = popularExercises;
        }
        
        public List<ExercisePopularity> getPopularExercises() { return popularExercises; }
        
        @Override
        public String toString() {
            return String.format("Popüler Egzersizler (%d adet)", popularExercises.size());
        }
    }
    
    /**
     * Egzersiz popülerliği için yardımcı sınıf
     */
    public static class ExercisePopularity {
        private final Exercise exercise;
        private final int popularityScore;
        
        public ExercisePopularity(Exercise exercise, int popularityScore) {
            this.exercise = exercise;
            this.popularityScore = popularityScore;
        }
        
        public Exercise getExercise() { return exercise; }
        public int getPopularityScore() { return popularityScore; }
        
        @Override
        public String toString() {
            return String.format("%s (Skor: %d)", exercise.getName(), popularityScore);
        }
    }
    
    /**
     * Haftalık özet raporu için yardımcı sınıf
     */
    public static class WeeklySummaryReport {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int activeUsers;
        private final int totalWorkouts;
        private final int totalDuration;
        private final double averageWorkoutsPerDay;
        
        public WeeklySummaryReport(LocalDate startDate, LocalDate endDate, int activeUsers,
                                 int totalWorkouts, int totalDuration, double averageWorkoutsPerDay) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.activeUsers = activeUsers;
            this.totalWorkouts = totalWorkouts;
            this.totalDuration = totalDuration;
            this.averageWorkoutsPerDay = averageWorkoutsPerDay;
        }
        
        // Getters
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getActiveUsers() { return activeUsers; }
        public int getTotalWorkouts() { return totalWorkouts; }
        public int getTotalDuration() { return totalDuration; }
        public double getAverageWorkoutsPerDay() { return averageWorkoutsPerDay; }
        
        @Override
        public String toString() {
            return String.format("Haftalık Özet: %d aktif kullanıcı, %d antrenman, %.1f günlük ortalama",
                    activeUsers, totalWorkouts, averageWorkoutsPerDay);
        }
    }
} 