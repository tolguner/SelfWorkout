package com.example.selfworkout.service;

import com.example.selfworkout.dao.EquipmentDAO;
import com.example.selfworkout.dao.ExerciseEquipmentDAO;
import com.example.selfworkout.model.Equipment;
import com.example.selfworkout.model.ExerciseEquipment;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Equipment için Business Logic sınıfı
 * Ekipman yönetimi işlemlerini koordine eder
 */
public class EquipmentService {
    
    private final EquipmentDAO equipmentDAO;
    private final ExerciseEquipmentDAO exerciseEquipmentDAO;
    
    public EquipmentService() {
        this.equipmentDAO = new EquipmentDAO();
        this.exerciseEquipmentDAO = new ExerciseEquipmentDAO();
    }
    
    /**
     * Yeni ekipman oluşturur
     */
    public Equipment createEquipment(Equipment equipment) throws SQLException {
        // Validasyon
        if (equipment.getName() == null || equipment.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ekipman adı boş olamaz!");
        }
        
        // Aynı isimde ekipman var mı kontrol et
        Equipment existingEquipment = equipmentDAO.findByName(equipment.getName());
        if (existingEquipment != null) {
            throw new IllegalArgumentException("Bu isimde bir ekipman zaten mevcut!");
        }
        
        Equipment savedEquipment = equipmentDAO.save(equipment);
        System.out.println("✅ Ekipman başarıyla oluşturuldu: " + savedEquipment.getName());
        
        return savedEquipment;
    }
    
    /**
     * Ekipmanı günceller
     */
    public boolean updateEquipment(Equipment equipment) throws SQLException {
        // Validasyon
        if (equipment.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir ekipman ID'si gerekli!");
        }
        
        if (equipment.getName() == null || equipment.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ekipman adı boş olamaz!");
        }
        
        // Ekipman var mı kontrol et
        Equipment existingEquipment = equipmentDAO.findById(equipment.getId());
        if (existingEquipment == null) {
            throw new IllegalArgumentException("Güncellenecek ekipman bulunamadı!");
        }
        
        return equipmentDAO.update(equipment);
    }
    
    /**
     * Ekipmanı siler
     */
    public boolean deleteEquipment(int equipmentId) throws SQLException {
        // Ekipman var mı kontrol et
        Equipment equipment = equipmentDAO.findById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("Silinecek ekipman bulunamadı!");
        }
        
        // Bu ekipmanı kullanan egzersizler var mı kontrol et
        List<ExerciseEquipment> relatedExercises = exerciseEquipmentDAO.findByEquipmentId(equipmentId);
        if (!relatedExercises.isEmpty()) {
            throw new IllegalArgumentException("Bu ekipmanı kullanan egzersizler var! Önce onları güncelleyin.");
        }
        
        boolean deleted = equipmentDAO.delete(equipmentId);
        
        if (deleted) {
            System.out.println("✅ Ekipman başarıyla silindi: " + equipment.getName());
        }
        
        return deleted;
    }
    
    /**
     * Tüm ekipmanları getirir
     */
    public List<Equipment> getAllEquipment() throws SQLException {
        return equipmentDAO.findAll();
    }
    
    /**
     * ID'ye göre ekipman getirir
     */
    public Equipment getEquipmentById(int equipmentId) throws SQLException {
        if (equipmentId <= 0) {
            throw new IllegalArgumentException("Geçerli bir ekipman ID'si gerekli!");
        }
        
        return equipmentDAO.findById(equipmentId);
    }
    
    /**
     * İsme göre ekipman arar
     */
    public Equipment searchEquipmentByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Arama terimi boş olamaz!");
        }
        
        return equipmentDAO.findByName(name);
    }
    
    /**
     * Ekipman sayısını getirir
     */
    public int getEquipmentCount() throws SQLException {
        return equipmentDAO.findAll().size();
    }
    
    /**
     * Ekipman kategorilerine göre filtreler
     */
    public List<Equipment> getEquipmentByCategory(String category) throws SQLException {
        if (category == null || category.trim().isEmpty()) {
            return getAllEquipment();
        }
        
        List<Equipment> allEquipment = getAllEquipment();
        String lowerCategory = category.toLowerCase();
        
        return allEquipment.stream()
            .filter(equipment -> {
                String equipmentName = equipment.getName().toLowerCase();
                String description = equipment.getDescription() != null ? 
                    equipment.getDescription().toLowerCase() : "";
                
                // Basit kategori mantığı
                switch (lowerCategory) {
                    case "ağırlık":
                    case "weight":
                        return equipmentName.contains("ağırlık") || equipmentName.contains("dumbbell") ||
                               equipmentName.contains("barbell") || equipmentName.contains("weight") ||
                               equipmentName.contains("halter");
                    case "kardio":
                    case "cardio":
                        return equipmentName.contains("koşu") || equipmentName.contains("bisiklet") ||
                               equipmentName.contains("treadmill") || equipmentName.contains("bike") ||
                               equipmentName.contains("elliptical");
                    case "makine":
                    case "machine":
                        return equipmentName.contains("makine") || equipmentName.contains("machine") ||
                               description.contains("makine") || description.contains("machine");
                    case "serbest":
                    case "free":
                        return equipmentName.contains("serbest") || equipmentName.contains("free") ||
                               equipmentName.contains("dumbbell") || equipmentName.contains("barbell");
                    default:
                        return equipmentName.contains(lowerCategory) || description.contains(lowerCategory);
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Ekipmanın egzersiz sayısını getirir
     */
    public int getExerciseCountForEquipment(int equipmentId) throws SQLException {
        if (equipmentId <= 0) {
            throw new IllegalArgumentException("Geçerli bir ekipman ID'si gerekli!");
        }
        
        List<ExerciseEquipment> exercises = exerciseEquipmentDAO.findByEquipmentId(equipmentId);
        return exercises.size();
    }
    
    /**
     * En popüler ekipmanları getirir (egzersiz sayısına göre)
     */
    public List<EquipmentStats> getPopularEquipment(int limit) throws SQLException {
        List<Equipment> allEquipment = getAllEquipment();
        
        return allEquipment.stream()
            .map(equipment -> {
                try {
                    int exerciseCount = getExerciseCountForEquipment(equipment.getId());
                    return new EquipmentStats(equipment, exerciseCount);
                } catch (SQLException e) {
                    System.err.println("Ekipman istatistiği alınırken hata: " + e.getMessage());
                    return new EquipmentStats(equipment, 0);
                }
            })
            .sorted((stats1, stats2) -> Integer.compare(stats2.getExerciseCount(), stats1.getExerciseCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Ekipman önerileri
     */
    public List<String> suggestEquipment() {
        return List.of(
            "Dumbbell", "Barbell", "Kettlebell", "Resistance Band",
            "Pull-up Bar", "Bench", "Cable Machine", "Smith Machine",
            "Treadmill", "Stationary Bike", "Elliptical", "Rowing Machine",
            "Medicine Ball", "Foam Roller", "Yoga Mat", "Suspension Trainer"
        );
    }
    
    /**
     * Ekipman validasyonu
     */
    public boolean isValidEquipment(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Minimum uzunluk kontrolü
        if (name.trim().length() < 2) {
            return false;
        }
        
        // Özel karakterler kontrolü (basit)
        return name.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ0-9\\s-]+$");
    }
    
    /**
     * Ekipman istatistikleri için yardımcı sınıf
     */
    public static class EquipmentStats {
        private final Equipment equipment;
        private final int exerciseCount;
        
        public EquipmentStats(Equipment equipment, int exerciseCount) {
            this.equipment = equipment;
            this.exerciseCount = exerciseCount;
        }
        
        public Equipment getEquipment() { return equipment; }
        public int getExerciseCount() { return exerciseCount; }
        
        @Override
        public String toString() {
            return String.format("%s (%d egzersiz)", equipment.getName(), exerciseCount);
        }
    }
} 