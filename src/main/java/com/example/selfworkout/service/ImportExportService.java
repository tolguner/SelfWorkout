package com.example.selfworkout.service;

import com.example.selfworkout.dao.*;
import com.example.selfworkout.model.*;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Import/Export için Business Logic sınıfı
 * Veri içe/dışa aktarma işlemlerini yönetir
 */
public class ImportExportService {
    
    private final UserDAO userDAO;
    private final ExerciseDAO exerciseDAO;
    private final DailyWorkoutDAO dailyWorkoutDAO;
    private final BodyStatsDAO bodyStatsDAO;
    private final ExerciseRoutineDAO routineDAO;
    
    private static final String CSV_SEPARATOR = ",";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ImportExportService() {
        this.userDAO = new UserDAO();
        this.exerciseDAO = new ExerciseDAO();
        this.dailyWorkoutDAO = new DailyWorkoutDAO();
        this.bodyStatsDAO = new BodyStatsDAO();
        this.routineDAO = new ExerciseRoutineDAO();
    }
    
    /**
     * Kullanıcı verilerini CSV formatında dışa aktarır
     */
    public ExportResult exportUsersToCSV(String filePath) throws SQLException, IOException {
        List<User> users = userDAO.findAll();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("ID,Username,Email,Name,Surname,Birthdate,CreatedAt");
            
            // Data
            for (User user : users) {
                writer.println(String.format("%d,%s,%s,%s,%s,%s,%s",
                    user.getId(),
                    escapeCSV(user.getUsername()),
                    escapeCSV(user.getEmail()),
                    escapeCSV(user.getName()),
                    escapeCSV(user.getSurname()),
                    user.getBirthdate() != null ? user.getBirthdate().format(DATE_FORMATTER) : "",
                    user.getCreatedAt() != null ? user.getCreatedAt().format(DATETIME_FORMATTER) : ""
                ));
            }
        }
        
        return new ExportResult(true, users.size(), "Kullanıcılar başarıyla dışa aktarıldı: " + filePath);
    }
    
    /**
     * Egzersiz verilerini CSV formatında dışa aktarır
     */
    public ExportResult exportExercisesToCSV(String filePath) throws SQLException, IOException {
        List<Exercise> exercises = exerciseDAO.findAll();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("ID,Name,Description,DifficultyLevel,Instructions");
            
            // Data
            for (Exercise exercise : exercises) {
                writer.println(String.format("%d,%s,%s,%s,%s",
                    exercise.getId(),
                    escapeCSV(exercise.getName()),
                    escapeCSV(exercise.getDescription()),
                    escapeCSV(exercise.getDifficultyLevel()),
                    escapeCSV(exercise.getInstructions())
                ));
            }
        }
        
        return new ExportResult(true, exercises.size(), "Egzersizler başarıyla dışa aktarıldı: " + filePath);
    }
    
    /**
     * Vücut istatistiklerini CSV formatında dışa aktarır
     */
    public ExportResult exportBodyStatsToCSV(String filePath, int userId) throws SQLException, IOException {
        List<BodyStats> bodyStats = bodyStatsDAO.findByUserId(userId);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("ID,UserID,Weight,Height,BodyFat,MuscleMass,RecordDate");
            
            // Data
            for (BodyStats stats : bodyStats) {
                writer.println(String.format("%d,%d,%.2f,%.2f,%.2f,%.2f,%s",
                    stats.getId(),
                    stats.getUserId(),
                    stats.getWeight(),
                    stats.getHeight(),
                    stats.getBodyFat(),
                    stats.getMuscleMass(),
                    stats.getRecordDate().format(DATE_FORMATTER)
                ));
            }
        }
        
        return new ExportResult(true, bodyStats.size(), "Vücut istatistikleri başarıyla dışa aktarıldı: " + filePath);
    }
    
    /**
     * Antrenman verilerini CSV formatında dışa aktarır
     */
    public ExportResult exportWorkoutsToCSV(String filePath, int userId) throws SQLException, IOException {
        List<DailyWorkout> workouts = dailyWorkoutDAO.findByUserId(userId);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("ID,UserID,WorkoutDate,TotalDuration,Notes");
            
            // Data
            for (DailyWorkout workout : workouts) {
                writer.println(String.format("%d,%d,%s,%d,%s",
                    workout.getId(),
                    workout.getUserId(),
                    workout.getWorkoutDate().format(DATE_FORMATTER),
                    workout.getTotalDuration(),
                    escapeCSV(workout.getNotes())
                ));
            }
        }
        
        return new ExportResult(true, workouts.size(), "Antrenmanlar başarıyla dışa aktarıldı: " + filePath);
    }
    
    /**
     * Rutinleri CSV formatında dışa aktarır
     */
    public ExportResult exportRoutinesToCSV(String filePath, int userId) throws SQLException, IOException {
        List<ExerciseRoutine> routines = routineDAO.findByUserId(userId);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("ID,UserID,Name,Description,CreatedAt");
            
            // Data
            for (ExerciseRoutine routine : routines) {
                writer.println(String.format("%d,%d,%s,%s,%s",
                    routine.getId(),
                    routine.getUserId(),
                    escapeCSV(routine.getName()),
                    escapeCSV(routine.getDescription()),
                    routine.getCreatedAt() != null ? routine.getCreatedAt().format(DATETIME_FORMATTER) : ""
                ));
            }
        }
        
        return new ExportResult(true, routines.size(), "Rutinler başarıyla dışa aktarıldı: " + filePath);
    }
    
    /**
     * Kullanıcının tüm verilerini dışa aktarır
     */
    public ExportResult exportUserDataToCSV(String directoryPath, int userId) throws SQLException, IOException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return new ExportResult(false, 0, "Kullanıcı bulunamadı!");
        }
        
        String userDir = directoryPath + File.separator + "user_" + userId + "_" + user.getUsername();
        File dir = new File(userDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        int totalRecords = 0;
        StringBuilder messages = new StringBuilder();
        
        try {
            // Vücut istatistikleri
            ExportResult bodyStatsResult = exportBodyStatsToCSV(userDir + File.separator + "body_stats.csv", userId);
            totalRecords += bodyStatsResult.getRecordCount();
            messages.append(bodyStatsResult.getMessage()).append("\n");
            
            // Antrenmanlar
            ExportResult workoutsResult = exportWorkoutsToCSV(userDir + File.separator + "workouts.csv", userId);
            totalRecords += workoutsResult.getRecordCount();
            messages.append(workoutsResult.getMessage()).append("\n");
            
            // Rutinler
            ExportResult routinesResult = exportRoutinesToCSV(userDir + File.separator + "routines.csv", userId);
            totalRecords += routinesResult.getRecordCount();
            messages.append(routinesResult.getMessage()).append("\n");
            
            return new ExportResult(true, totalRecords, 
                String.format("Kullanıcı verileri başarıyla dışa aktarıldı: %s\n%s", userDir, messages.toString()));
            
        } catch (Exception e) {
            return new ExportResult(false, totalRecords, "Dışa aktarma hatası: " + e.getMessage());
        }
    }
    
    /**
     * Vücut istatistiklerini CSV'den içe aktarır
     */
    public ImportResult importBodyStatsFromCSV(String filePath, int userId) throws IOException, SQLException {
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Header'ı atla
            
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(CSV_SEPARATOR);
                    if (parts.length >= 6) {
                        BodyStats bodyStats = new BodyStats();
                        bodyStats.setUserId(userId);
                        bodyStats.setWeight(Double.parseDouble(parts[2]));
                        bodyStats.setHeight(Double.parseDouble(parts[3]));
                        bodyStats.setBodyFat(Double.parseDouble(parts[4]));
                        bodyStats.setMuscleMass(Double.parseDouble(parts[5]));
                        bodyStats.setRecordDate(LocalDate.parse(parts[6], DATE_FORMATTER));
                        
                        bodyStatsDAO.save(bodyStats);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.append("Satır hatası: ").append(line).append(" - ").append(e.getMessage()).append("\n");
                }
            }
        }
        
        return new ImportResult(successCount, errorCount, 
            String.format("İçe aktarma tamamlandı. Başarılı: %d, Hatalı: %d", successCount, errorCount),
            errors.toString());
    }
    
    /**
     * Backup oluşturur
     */
    public ExportResult createBackup(String backupPath) throws SQLException, IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = backupPath + File.separator + "backup_" + timestamp;
        
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        int totalRecords = 0;
        StringBuilder messages = new StringBuilder();
        
        try {
            // Kullanıcıları dışa aktar
            ExportResult usersResult = exportUsersToCSV(backupDir + File.separator + "users.csv");
            totalRecords += usersResult.getRecordCount();
            messages.append(usersResult.getMessage()).append("\n");
            
            // Egzersizleri dışa aktar
            ExportResult exercisesResult = exportExercisesToCSV(backupDir + File.separator + "exercises.csv");
            totalRecords += exercisesResult.getRecordCount();
            messages.append(exercisesResult.getMessage()).append("\n");
            
            return new ExportResult(true, totalRecords, 
                String.format("Yedekleme başarıyla oluşturuldu: %s\n%s", backupDir, messages.toString()));
            
        } catch (Exception e) {
            return new ExportResult(false, totalRecords, "Yedekleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * CSV değerlerini escape eder
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Dışa aktarma sonucu için yardımcı sınıf
     */
    public static class ExportResult {
        private final boolean success;
        private final int recordCount;
        private final String message;
        
        public ExportResult(boolean success, int recordCount, String message) {
            this.success = success;
            this.recordCount = recordCount;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public int getRecordCount() { return recordCount; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("Export %s: %d kayıt - %s", 
                success ? "Başarılı" : "Başarısız", recordCount, message);
        }
    }
    
    /**
     * İçe aktarma sonucu için yardımcı sınıf
     */
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;
        private final String message;
        private final String errors;
        
        public ImportResult(int successCount, int errorCount, String message, String errors) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.message = message;
            this.errors = errors;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public String getMessage() { return message; }
        public String getErrors() { return errors; }
        public boolean isSuccess() { return errorCount == 0; }
        
        @Override
        public String toString() {
            return String.format("Import: %d başarılı, %d hatalı - %s", 
                successCount, errorCount, message);
        }
    }
} 