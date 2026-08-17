package com.example.selfworkout.dao;

import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Raporlama ve istatistik sorguları için Data Access Object sınıfı.
 *
 * Diğer DAO'lardan farklı olarak tek bir tabloya değil, veritabanındaki
 * görünüm (view), fonksiyon ve saklanan yordamlara karşılık gelir.
 * Bu nesneler src/main/sql/selfworkout_objects.sql içinde tanımlıdır.
 */
public class ReportingDAO {

    // SQL sorguları
    private static final String SELECT_POPULAR_EXERCISES =
            "SELECT TOP (?) exercise_id, exercise_name, difficulty_level, " +
                    "usage_count, favorite_count, routine_count " +
                    "FROM vw_PopularExercises " +
                    "ORDER BY usage_count DESC, favorite_count DESC, routine_count DESC";

    private static final String SELECT_WORKOUT_SUMMARY_BY_USER =
            "SELECT user_workout_id, workout_date, workout_type, status, " +
                    "duration_minutes, exercise_count, total_sets, total_volume " +
                    "FROM vw_UserWorkoutSummary " +
                    "WHERE user_id = ? ORDER BY workout_date DESC";

    private static final String SELECT_MONTHLY_PROGRESS =
            "SELECT yil, ay, workout_count, total_volume, avg_duration_minutes " +
                    "FROM vw_UserMonthlyProgress " +
                    "WHERE user_id = ? ORDER BY yil DESC, ay DESC";

    private static final String SELECT_WORKOUT_VOLUME =
            "SELECT dbo.fn_WorkoutVolume(?) AS volume";

    private static final String SELECT_ONE_REP_MAX =
            "SELECT dbo.fn_EstimatedOneRepMax(?, ?) AS one_rm";

    private static final String SELECT_CURRENT_STREAK =
            "SELECT dbo.fn_WorkoutStreak(?) AS streak";

    private static final String SELECT_LONGEST_STREAK =
            "SELECT dbo.fn_LongestWorkoutStreak(?) AS streak";

    private static final String CALL_DASHBOARD_STATS =
            "{call sp_UserDashboardStats(?)}";

    /**
     * En çok kullanılan egzersizleri gerçek kullanım verisine göre döndürür.
     */
    public List<PopularExercise> findPopularExercises(int limit) throws SQLException {
        List<PopularExercise> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_POPULAR_EXERCISES)) {

            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new PopularExercise(
                            resultSet.getInt("exercise_id"),
                            resultSet.getString("exercise_name"),
                            resultSet.getString("difficulty_level"),
                            resultSet.getInt("usage_count"),
                            resultSet.getInt("favorite_count"),
                            resultSet.getInt("routine_count")
                    ));
                }
            }
        }

        return results;
    }

    /**
     * Kullanıcının antrenman özetlerini döndürür (süre, set sayısı, hacim).
     */
    public List<WorkoutSummary> findWorkoutSummaries(int userId) throws SQLException {
        List<WorkoutSummary> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_WORKOUT_SUMMARY_BY_USER)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new WorkoutSummary(
                            resultSet.getInt("user_workout_id"),
                            resultSet.getDate("workout_date").toLocalDate(),
                            resultSet.getString("workout_type"),
                            resultSet.getString("status"),
                            resultSet.getInt("duration_minutes"),
                            resultSet.getInt("exercise_count"),
                            resultSet.getInt("total_sets"),
                            resultSet.getDouble("total_volume")
                    ));
                }
            }
        }

        return results;
    }

    /**
     * Kullanıcının aylık ilerlemesini döndürür.
     */
    public List<MonthlyProgress> findMonthlyProgress(int userId) throws SQLException {
        List<MonthlyProgress> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_MONTHLY_PROGRESS)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new MonthlyProgress(
                            resultSet.getInt("yil"),
                            resultSet.getInt("ay"),
                            resultSet.getInt("workout_count"),
                            resultSet.getDouble("total_volume"),
                            resultSet.getDouble("avg_duration_minutes")
                    ));
                }
            }
        }

        return results;
    }

    /**
     * Bir antrenmanın toplam hacmini döndürür (tekrar x ağırlık).
     */
    public double findWorkoutVolume(int userWorkoutId) throws SQLException {
        return querySingleDouble(SELECT_WORKOUT_VOLUME, userWorkoutId);
    }

    /**
     * Kullanıcının bir egzersizdeki tahmini tek tekrar maksimumunu döndürür.
     */
    public double findEstimatedOneRepMax(int userId, int exerciseId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ONE_REP_MAX)) {

            statement.setInt(1, userId);
            statement.setInt(2, exerciseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getDouble(1) : 0.0;
            }
        }
    }

    /**
     * Kullanıcının güncel kesintisiz antrenman serisi (gün).
     */
    public int findCurrentStreak(int userId) throws SQLException {
        return (int) querySingleDouble(SELECT_CURRENT_STREAK, userId);
    }

    /**
     * Kullanıcının şimdiye kadarki en uzun antrenman serisi (gün).
     */
    public int findLongestStreak(int userId) throws SQLException {
        return (int) querySingleDouble(SELECT_LONGEST_STREAK, userId);
    }

    /**
     * Kullanıcı panosu istatistiklerini tek sorguda döndürür.
     */
    public DashboardStats findDashboardStats(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             CallableStatement statement = connection.prepareCall(CALL_DASHBOARD_STATS)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new DashboardStats(
                            resultSet.getInt("tamamlanan_antrenman"),
                            resultSet.getInt("rutin_sayisi"),
                            resultSet.getInt("favori_sayisi"),
                            resultSet.getInt("guncel_seri"),
                            resultSet.getInt("en_uzun_seri"),
                            resultSet.getDouble("vucut_kitle_indeksi"),
                            resultSet.getDouble("toplam_hacim")
                    );
                }
            }
        }

        return null;
    }

    /**
     * Tek parametreli, tek sayısal değer dönen sorgular için ortak yardımcı.
     */
    private double querySingleDouble(String sql, int parameter) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getDouble(1) : 0.0;
            }
        }
    }


    /* ================= Sonuç taşıyıcı sınıflar ================= */

    /**
     * vw_PopularExercises satırı.
     */
    public static class PopularExercise {
        private final int exerciseId;
        private final String exerciseName;
        private final String difficultyLevel;
        private final int usageCount;
        private final int favoriteCount;
        private final int routineCount;

        public PopularExercise(int exerciseId, String exerciseName, String difficultyLevel,
                               int usageCount, int favoriteCount, int routineCount) {
            this.exerciseId = exerciseId;
            this.exerciseName = exerciseName;
            this.difficultyLevel = difficultyLevel;
            this.usageCount = usageCount;
            this.favoriteCount = favoriteCount;
            this.routineCount = routineCount;
        }

        public int getExerciseId() { return exerciseId; }
        public String getExerciseName() { return exerciseName; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public int getUsageCount() { return usageCount; }
        public int getFavoriteCount() { return favoriteCount; }
        public int getRoutineCount() { return routineCount; }

        /**
         * Popülerlik skoru: kayıtlı set sayısı en güçlü sinyal, favori ve
         * rutinde kullanım destekleyici sinyaller.
         */
        public int getPopularityScore() {
            return usageCount * 3 + favoriteCount * 2 + routineCount;
        }

        @Override
        public String toString() {
            return String.format("%s (kullanım: %d, favori: %d)",
                    exerciseName, usageCount, favoriteCount);
        }
    }

    /**
     * vw_UserWorkoutSummary satırı.
     */
    public static class WorkoutSummary {
        private final int userWorkoutId;
        private final java.time.LocalDate workoutDate;
        private final String workoutType;
        private final String status;
        private final int durationMinutes;
        private final int exerciseCount;
        private final int totalSets;
        private final double totalVolume;

        public WorkoutSummary(int userWorkoutId, java.time.LocalDate workoutDate, String workoutType,
                              String status, int durationMinutes, int exerciseCount,
                              int totalSets, double totalVolume) {
            this.userWorkoutId = userWorkoutId;
            this.workoutDate = workoutDate;
            this.workoutType = workoutType;
            this.status = status;
            this.durationMinutes = durationMinutes;
            this.exerciseCount = exerciseCount;
            this.totalSets = totalSets;
            this.totalVolume = totalVolume;
        }

        public int getUserWorkoutId() { return userWorkoutId; }
        public java.time.LocalDate getWorkoutDate() { return workoutDate; }
        public String getWorkoutType() { return workoutType; }
        public String getStatus() { return status; }
        public int getDurationMinutes() { return durationMinutes; }
        public int getExerciseCount() { return exerciseCount; }
        public int getTotalSets() { return totalSets; }
        public double getTotalVolume() { return totalVolume; }

        @Override
        public String toString() {
            return String.format("%s - %d set, %.1f kg hacim", workoutDate, totalSets, totalVolume);
        }
    }

    /**
     * vw_UserMonthlyProgress satırı.
     */
    public static class MonthlyProgress {
        private final int year;
        private final int month;
        private final int workoutCount;
        private final double totalVolume;
        private final double averageDurationMinutes;

        public MonthlyProgress(int year, int month, int workoutCount,
                               double totalVolume, double averageDurationMinutes) {
            this.year = year;
            this.month = month;
            this.workoutCount = workoutCount;
            this.totalVolume = totalVolume;
            this.averageDurationMinutes = averageDurationMinutes;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getWorkoutCount() { return workoutCount; }
        public double getTotalVolume() { return totalVolume; }
        public double getAverageDurationMinutes() { return averageDurationMinutes; }

        @Override
        public String toString() {
            return String.format("%d/%02d - %d antrenman, %.1f kg hacim",
                    year, month, workoutCount, totalVolume);
        }
    }

    /**
     * sp_UserDashboardStats sonucu.
     */
    public static class DashboardStats {
        private final int completedWorkouts;
        private final int routineCount;
        private final int favoriteCount;
        private final int currentStreak;
        private final int longestStreak;
        private final double bmi;
        private final double totalVolume;

        public DashboardStats(int completedWorkouts, int routineCount, int favoriteCount,
                              int currentStreak, int longestStreak, double bmi, double totalVolume) {
            this.completedWorkouts = completedWorkouts;
            this.routineCount = routineCount;
            this.favoriteCount = favoriteCount;
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
            this.bmi = bmi;
            this.totalVolume = totalVolume;
        }

        public int getCompletedWorkouts() { return completedWorkouts; }
        public int getRoutineCount() { return routineCount; }
        public int getFavoriteCount() { return favoriteCount; }
        public int getCurrentStreak() { return currentStreak; }
        public int getLongestStreak() { return longestStreak; }
        public double getBmi() { return bmi; }
        public double getTotalVolume() { return totalVolume; }

        @Override
        public String toString() {
            return String.format("%d antrenman, %d gün seri, %.1f kg toplam hacim",
                    completedWorkouts, currentStreak, totalVolume);
        }
    }
}
