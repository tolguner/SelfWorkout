package com.example.selfworkout.service;

import com.example.selfworkout.dao.LogDAO;
import com.example.selfworkout.model.Log;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Log için Business Logic sınıfı
 * Uygulama log yönetimi işlemlerini koordine eder
 */
public class LogService {
    
    private final LogDAO logDAO;
    
    public LogService() {
        this.logDAO = new LogDAO();
    }
    
    /**
     * Yeni log kaydı oluşturur
     */
    public Log createLog(String action, String description) throws SQLException {
        return createLog(action, description, null);
    }
    
    /**
     * Kullanıcı ile ilişkili log kaydı oluşturur
     */
    public Log createLog(String action, String description, Integer userId) throws SQLException {
        // Validasyon
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Log aksiyonu gerekli!");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Log açıklaması gerekli!");
        }
        
        // Log oluştur
        Log log = new Log();
        log.setAction(action.toUpperCase());
        log.setDescription(description);
        log.setUserId(userId);
        log.setTimestamp(LocalDateTime.now());
        
        return logDAO.save(log);
    }
    
    /**
     * INFO seviyesinde log kaydı oluşturur
     */
    public Log logInfo(String description, String action) throws SQLException {
        return createLog("INFO_" + action, description);
    }
    
    /**
     * WARNING seviyesinde log kaydı oluşturur
     */
    public Log logWarning(String description, String action) throws SQLException {
        return createLog("WARNING_" + action, description);
    }
    
    /**
     * ERROR seviyesinde log kaydı oluşturur
     */
    public Log logError(String description, String action) throws SQLException {
        return createLog("ERROR_" + action, description);
    }
    
    /**
     * DEBUG seviyesinde log kaydı oluşturur
     */
    public Log logDebug(String description, String action) throws SQLException {
        return createLog("DEBUG_" + action, description);
    }
    
    /**
     * Kullanıcı aktivitesi log'u oluşturur
     */
    public Log logUserActivity(int userId, String activity, String details) throws SQLException {
        String description = String.format("Kullanıcı Aktivitesi: %s - %s", activity, details);
        return createLog("USER_ACTIVITY", description, userId);
    }
    
    /**
     * Sistem hatası log'u oluşturur
     */
    public Log logSystemError(String error, String stackTrace) throws SQLException {
        String description = String.format("Sistem Hatası: %s\nStack Trace: %s", error, stackTrace);
        return createLog("SYSTEM_ERROR", description);
    }
    
    /**
     * Tüm log kayıtlarını getirir
     */
    public List<Log> getAllLogs() throws SQLException {
        return logDAO.findAll();
    }
    
    /**
     * Aksiyona göre log kayıtlarını getirir
     */
    public List<Log> getLogsByAction(String action) throws SQLException {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Log aksiyonu gerekli!");
        }
        
        // Basit implementasyon - tüm logları al ve filtrele
        List<Log> allLogs = logDAO.findAll();
        return allLogs.stream()
            .filter(log -> action.equalsIgnoreCase(log.getAction()))
            .collect(Collectors.toList());
    }
    
    /**
     * Kullanıcıya göre log kayıtlarını getirir
     */
    public List<Log> getLogsByUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return logDAO.findByUserId(userId);
    }
    
    /**
     * Tarih aralığına göre log kayıtlarını getirir
     */
    public List<Log> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri gerekli!");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Başlangıç tarihi bitiş tarihinden sonra olamaz!");
        }
        
        // Basit implementasyon - tüm logları al ve filtrele
        List<Log> allLogs = logDAO.findAll();
        return allLogs.stream()
            .filter(log -> !log.getTimestamp().isBefore(startDate) && 
                          !log.getTimestamp().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Son N adet log kaydını getirir
     */
    public List<Log> getRecentLogs(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit pozitif olmalı!");
        }
        
        List<Log> allLogs = logDAO.findAll();
        
        // Tarihe göre sırala (en yeni önce) ve limit uygula
        return allLogs.stream()
            .sorted((log1, log2) -> log2.getTimestamp().compareTo(log1.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Hata loglarını getirir
     */
    public List<Log> getErrorLogs() throws SQLException {
        List<Log> allLogs = logDAO.findAll();
        return allLogs.stream()
            .filter(log -> log.getAction().contains("ERROR"))
            .collect(Collectors.toList());
    }
    
    /**
     * Uyarı loglarını getirir
     */
    public List<Log> getWarningLogs() throws SQLException {
        List<Log> allLogs = logDAO.findAll();
        return allLogs.stream()
            .filter(log -> log.getAction().contains("WARNING"))
            .collect(Collectors.toList());
    }
    
    /**
     * Log açıklamasında arama yapar
     */
    public List<Log> searchLogs(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Arama terimi gerekli!");
        }
        
        List<Log> allLogs = logDAO.findAll();
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        return allLogs.stream()
            .filter(log -> log.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                          log.getAction().toLowerCase().contains(lowerSearchTerm))
            .collect(Collectors.toList());
    }
    
    /**
     * Log kaydını siler
     */
    public boolean deleteLog(int logId) throws SQLException {
        if (logId <= 0) {
            throw new IllegalArgumentException("Geçerli bir log ID'si gerekli!");
        }
        
        return logDAO.delete(logId);
    }
    
    /**
     * Belirli tarihten eski logları siler
     */
    public int deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        if (beforeDate == null) {
            throw new IllegalArgumentException("Tarih gerekli!");
        }
        
        List<Log> allLogs = logDAO.findAll();
        int deletedCount = 0;
        
        for (Log log : allLogs) {
            if (log.getTimestamp().isBefore(beforeDate)) {
                if (logDAO.delete(log.getId())) {
                    deletedCount++;
                }
            }
        }
        
        System.out.println("✅ " + deletedCount + " adet eski log kaydı silindi.");
        return deletedCount;
    }
    
    /**
     * Log istatistiklerini getirir
     */
    public LogStatistics getLogStatistics() throws SQLException {
        List<Log> allLogs = logDAO.findAll();
        
        int totalLogs = allLogs.size();
        int errorCount = (int) allLogs.stream().filter(log -> log.getAction().contains("ERROR")).count();
        int warningCount = (int) allLogs.stream().filter(log -> log.getAction().contains("WARNING")).count();
        int infoCount = (int) allLogs.stream().filter(log -> log.getAction().contains("INFO")).count();
        int debugCount = (int) allLogs.stream().filter(log -> log.getAction().contains("DEBUG")).count();
        
        return new LogStatistics(totalLogs, errorCount, warningCount, infoCount, debugCount);
    }
    
    /**
     * Log istatistikleri için yardımcı sınıf
     */
    public static class LogStatistics {
        private final int totalLogs;
        private final int errorCount;
        private final int warningCount;
        private final int infoCount;
        private final int debugCount;
        
        public LogStatistics(int totalLogs, int errorCount, int warningCount, int infoCount, int debugCount) {
            this.totalLogs = totalLogs;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.infoCount = infoCount;
            this.debugCount = debugCount;
        }
        
        public int getTotalLogs() { return totalLogs; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public int getInfoCount() { return infoCount; }
        public int getDebugCount() { return debugCount; }
        
        public double getErrorPercentage() {
            return totalLogs > 0 ? (double) errorCount / totalLogs * 100 : 0;
        }
        
        public double getWarningPercentage() {
            return totalLogs > 0 ? (double) warningCount / totalLogs * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("Log İstatistikleri: Toplam %d, Hata %d (%.1f%%), Uyarı %d (%.1f%%)", 
                    totalLogs, errorCount, getErrorPercentage(), warningCount, getWarningPercentage());
        }
    }
} 