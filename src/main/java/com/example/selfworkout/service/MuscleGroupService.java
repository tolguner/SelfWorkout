package com.example.selfworkout.service;

import com.example.selfworkout.dao.MuscleGroupDAO;
import com.example.selfworkout.dao.ExerciseMuscleDAO;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.model.ExerciseMuscle;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MuscleGroup için Business Logic sınıfı
 * Kas grubu yönetimi işlemlerini koordine eder
 */
public class MuscleGroupService {
    
    private final MuscleGroupDAO muscleGroupDAO;
    private final ExerciseMuscleDAO exerciseMuscleDAO;
    
    public MuscleGroupService() {
        this.muscleGroupDAO = new MuscleGroupDAO();
        this.exerciseMuscleDAO = new ExerciseMuscleDAO();
    }
    
    /**
     * Yeni kas grubu oluşturur
     */
    public MuscleGroup createMuscleGroup(MuscleGroup muscleGroup) throws SQLException {
        // Validasyon
        if (muscleGroup.getName() == null || muscleGroup.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Kas grubu adı boş olamaz!");
        }
        
        // Aynı isimde kas grubu var mı kontrol et
        MuscleGroup existingGroup = muscleGroupDAO.findByName(muscleGroup.getName());
        if (existingGroup != null) {
            throw new IllegalArgumentException("Bu isimde bir kas grubu zaten mevcut!");
        }
        
        MuscleGroup savedGroup = muscleGroupDAO.save(muscleGroup);
        System.out.println("✅ Kas grubu başarıyla oluşturuldu: " + savedGroup.getName());
        
        return savedGroup;
    }
    
    /**
     * Kas grubunu günceller
     */
    public boolean updateMuscleGroup(MuscleGroup muscleGroup) throws SQLException {
        // Validasyon
        if (muscleGroup.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kas grubu ID'si gerekli!");
        }
        
        if (muscleGroup.getName() == null || muscleGroup.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Kas grubu adı boş olamaz!");
        }
        
        // Kas grubu var mı kontrol et
        MuscleGroup existingGroup = muscleGroupDAO.findById(muscleGroup.getId());
        if (existingGroup == null) {
            throw new IllegalArgumentException("Güncellenecek kas grubu bulunamadı!");
        }
        
        return muscleGroupDAO.update(muscleGroup);
    }
    
    /**
     * Kas grubunu siler
     */
    public boolean deleteMuscleGroup(int muscleGroupId) throws SQLException {
        // Kas grubu var mı kontrol et
        MuscleGroup muscleGroup = muscleGroupDAO.findById(muscleGroupId);
        if (muscleGroup == null) {
            throw new IllegalArgumentException("Silinecek kas grubu bulunamadı!");
        }
        
        // Bu kas grubunu kullanan egzersizler var mı kontrol et (basit implementasyon)
        List<ExerciseMuscle> allExerciseMuscles = exerciseMuscleDAO.findAll();
        List<ExerciseMuscle> relatedExercises = allExerciseMuscles.stream()
            .filter(em -> em.getMuscleId() == muscleGroupId)
            .collect(Collectors.toList());
        
        if (!relatedExercises.isEmpty()) {
            throw new IllegalArgumentException("Bu kas grubunu kullanan egzersizler var! Önce onları güncelleyin.");
        }
        
        boolean deleted = muscleGroupDAO.delete(muscleGroupId);
        
        if (deleted) {
            System.out.println("✅ Kas grubu başarıyla silindi: " + muscleGroup.getName());
        }
        
        return deleted;
    }
    
    /**
     * Tüm kas gruplarını getirir
     */
    public List<MuscleGroup> getAllMuscleGroups() throws SQLException {
        return muscleGroupDAO.findAll();
    }
    
    /**
     * ID'ye göre kas grubu getirir
     */
    public MuscleGroup getMuscleGroupById(int muscleGroupId) throws SQLException {
        if (muscleGroupId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kas grubu ID'si gerekli!");
        }
        
        return muscleGroupDAO.findById(muscleGroupId);
    }
    
    /**
     * İsme göre kas grubu arar
     */
    public MuscleGroup searchMuscleGroupByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Arama terimi boş olamaz!");
        }
        
        return muscleGroupDAO.findByName(name);
    }
    
    /**
     * Kas grubu sayısını getirir
     */
    public int getMuscleGroupCount() throws SQLException {
        return muscleGroupDAO.findAll().size();
    }
    
    /**
     * Aktif kas gruplarını getirir
     */
    public List<MuscleGroup> getActiveMuscleGroups() throws SQLException {
        // Basit implementasyon - tüm kas gruplarını getir
        // Gerçek uygulamada "active" field'ı olabilir
        return getAllMuscleGroups();
    }
    
    /**
     * Kas grubunun egzersiz sayısını getirir
     */
    public int getExerciseCountForMuscleGroup(int muscleGroupId) throws SQLException {
        if (muscleGroupId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kas grubu ID'si gerekli!");
        }
        
        // Basit implementasyon - tüm ExerciseMuscle kayıtlarını al ve filtrele
        List<ExerciseMuscle> allExerciseMuscles = exerciseMuscleDAO.findAll();
        return (int) allExerciseMuscles.stream()
            .filter(em -> em.getMuscleId() == muscleGroupId)
            .count();
    }
    
    /**
     * En popüler kas gruplarını getirir (egzersiz sayısına göre)
     */
    public List<MuscleGroupStats> getPopularMuscleGroups(int limit) throws SQLException {
        List<MuscleGroup> allGroups = getAllMuscleGroups();
        
        return allGroups.stream()
            .map(group -> {
                try {
                    int exerciseCount = getExerciseCountForMuscleGroup(group.getId());
                    return new MuscleGroupStats(group, exerciseCount);
                } catch (SQLException e) {
                    System.err.println("Kas grubu istatistiği alınırken hata: " + e.getMessage());
                    return new MuscleGroupStats(group, 0);
                }
            })
            .sorted((stats1, stats2) -> Integer.compare(stats2.getExerciseCount(), stats1.getExerciseCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Kas grubu kategorilerine göre gruplar
     */
    public List<MuscleGroup> getMuscleGroupsByCategory(String category) throws SQLException {
        if (category == null || category.trim().isEmpty()) {
            return getAllMuscleGroups();
        }
        
        // Basit kategori filtreleme
        List<MuscleGroup> allGroups = getAllMuscleGroups();
        String lowerCategory = category.toLowerCase();
        
        return allGroups.stream()
            .filter(group -> {
                String groupName = group.getName().toLowerCase();
                // Basit kategori mantığı
                switch (lowerCategory) {
                    case "üst":
                    case "upper":
                        return groupName.contains("göğüs") || groupName.contains("sırt") || 
                               groupName.contains("omuz") || groupName.contains("kol") ||
                               groupName.contains("chest") || groupName.contains("back") ||
                               groupName.contains("shoulder") || groupName.contains("arm");
                    case "alt":
                    case "lower":
                        return groupName.contains("bacak") || groupName.contains("kalça") ||
                               groupName.contains("leg") || groupName.contains("glute");
                    case "core":
                    case "karın":
                        return groupName.contains("karın") || groupName.contains("core") ||
                               groupName.contains("abs");
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Kas grubu istatistikleri için yardımcı sınıf
     */
    public static class MuscleGroupStats {
        private final MuscleGroup muscleGroup;
        private final int exerciseCount;
        
        public MuscleGroupStats(MuscleGroup muscleGroup, int exerciseCount) {
            this.muscleGroup = muscleGroup;
            this.exerciseCount = exerciseCount;
        }
        
        public MuscleGroup getMuscleGroup() { return muscleGroup; }
        public int getExerciseCount() { return exerciseCount; }
        
        @Override
        public String toString() {
            return String.format("%s (%d egzersiz)", muscleGroup.getName(), exerciseCount);
        }
    }
    
    /**
     * Kas grubu validasyonu
     */
    public boolean isValidMuscleGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Minimum uzunluk kontrolü
        if (name.trim().length() < 2) {
            return false;
        }
        
        // Özel karakterler kontrolü (basit)
        return name.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]+$");
    }
    
    /**
     * Kas grubu önerisi
     */
    public List<String> suggestMuscleGroups() {
        return List.of(
            "Göğüs", "Sırt", "Omuz", "Biceps", "Triceps",
            "Ön Kol", "Karın", "Bacak", "Kalça", "Baldır"
        );
    }
} 