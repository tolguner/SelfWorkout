package com.example.selfworkout.service;

import com.example.selfworkout.dao.ExerciseDAO;
import com.example.selfworkout.dao.MuscleGroupDAO;
import com.example.selfworkout.dao.EquipmentDAO;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.model.MuscleGroup;
import com.example.selfworkout.model.Equipment;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Exercise yönetimi için Service sınıfı
 */
public class ExerciseService {
    
    private final ExerciseDAO exerciseDAO;
    private final MuscleGroupDAO muscleGroupDAO;
    private final EquipmentDAO equipmentDAO;
    private final AuthenticationService authService;
    
    public ExerciseService(AuthenticationService authService) {
        this.exerciseDAO = new ExerciseDAO();
        this.muscleGroupDAO = new MuscleGroupDAO();
        this.equipmentDAO = new EquipmentDAO();
        this.authService = authService;
    }
    
    /**
     * Yeni egzersiz oluşturur
     */
    public boolean createExercise(String name, String description, String difficultyLevel, 
                                String instructions, List<Integer> muscleGroupIds, 
                                List<Integer> equipmentIds) {
        
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            // Exercise oluştur
            Exercise exercise = new Exercise();
            exercise.setName(name.trim());
            exercise.setDescription(description != null ? description.trim() : "");
            exercise.setDifficultyLevel(difficultyLevel);
            exercise.setInstructions(instructions != null ? instructions.trim() : "");
            exercise.setCreatedBy(authService.getCurrentUserId());
            
            // Kas gruplarını ayarla
            if (muscleGroupIds != null && !muscleGroupIds.isEmpty()) {
                List<MuscleGroup> muscleGroups = new ArrayList<>();
                for (Integer muscleGroupId : muscleGroupIds) {
                    MuscleGroup muscleGroup = muscleGroupDAO.findById(muscleGroupId);
                    if (muscleGroup != null) {
                        muscleGroups.add(muscleGroup);
                    }
                }
                exercise.setMuscleGroups(muscleGroups);
            }
            
            // Ekipmanları ayarla
            if (equipmentIds != null && !equipmentIds.isEmpty()) {
                List<Equipment> equipments = new ArrayList<>();
                for (Integer equipmentId : equipmentIds) {
                    Equipment equipment = equipmentDAO.findById(equipmentId);
                    if (equipment != null) {
                        equipments.add(equipment);
                    }
                }
                exercise.setEquipments(equipments);
            }
            
            // Kaydet
            Exercise savedExercise = exerciseDAO.save(exercise);
            
            if (savedExercise != null) {
                System.out.println("✅ Egzersiz başarıyla oluşturuldu: " + name);
                return true;
            } else {
                System.out.println("❌ Egzersiz oluşturulamadı!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz oluşturma hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Egzersizi günceller
     */
    public boolean updateExercise(int exerciseId, String name, String description, 
                                String difficultyLevel, String instructions,
                                List<Integer> muscleGroupIds, List<Integer> equipmentIds) {
        
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            // Egzersizi bul
            Exercise exercise = exerciseDAO.findById(exerciseId);
            if (exercise == null) {
                System.out.println("❌ Egzersiz bulunamadı!");
                return false;
            }
            
            // Yetki kontrolü (sadece admin veya oluşturan kişi)
            if (!authService.isCurrentUserAdmin() && 
                !Objects.equals(exercise.getCreatedBy(), authService.getCurrentUserId())) {
                System.out.println("❌ Bu egzersizi güncelleme yetkiniz yok!");
                return false;
            }
            
            // Validation
            if (!isValidExerciseName(name)) {
                return false;
            }
            
            if (!isValidDifficultyLevel(difficultyLevel)) {
                return false;
            }
            
            // Güncelle
            exercise.setName(name.trim());
            exercise.setDescription(description != null ? description.trim() : "");
            exercise.setDifficultyLevel(difficultyLevel);
            exercise.setInstructions(instructions != null ? instructions.trim() : "");
            
            // Kas gruplarını güncelle
            if (muscleGroupIds != null) {
                List<MuscleGroup> muscleGroups = new ArrayList<>();
                for (Integer muscleGroupId : muscleGroupIds) {
                    MuscleGroup muscleGroup = muscleGroupDAO.findById(muscleGroupId);
                    if (muscleGroup != null) {
                        muscleGroups.add(muscleGroup);
                    }
                }
                exercise.setMuscleGroups(muscleGroups);
            }
            
            // Ekipmanları güncelle
            if (equipmentIds != null) {
                List<Equipment> equipments = new ArrayList<>();
                for (Integer equipmentId : equipmentIds) {
                    Equipment equipment = equipmentDAO.findById(equipmentId);
                    if (equipment != null) {
                        equipments.add(equipment);
                    }
                }
                exercise.setEquipments(equipments);
            }
            
            boolean success = exerciseDAO.update(exercise);
            
            if (success) {
                System.out.println("✅ Egzersiz başarıyla güncellendi: " + name);
                return true;
            } else {
                System.out.println("❌ Egzersiz güncellenemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Egzersizi siler
     */
    public boolean deleteExercise(int exerciseId) {
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            // Egzersizi bul
            Exercise exercise = exerciseDAO.findById(exerciseId);
            if (exercise == null) {
                System.out.println("❌ Egzersiz bulunamadı!");
                return false;
            }
            
            // Yetki kontrolü (sadece admin veya oluşturan kişi)
            if (!authService.isCurrentUserAdmin() && 
                !Objects.equals(exercise.getCreatedBy(), authService.getCurrentUserId())) {
                System.out.println("❌ Bu egzersizi silme yetkiniz yok!");
                return false;
            }
            
            boolean success = exerciseDAO.delete(exerciseId);
            
            if (success) {
                System.out.println("✅ Egzersiz başarıyla silindi: " + exercise.getName());
                return true;
            } else {
                System.out.println("❌ Egzersiz silinemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz silme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tüm egzersizleri getirir
     */
    public List<Exercise> getAllExercises() {
        try {
            return exerciseDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Egzersizler yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * ID'ye göre egzersiz bulur
     */
    public Exercise getExerciseById(int exerciseId) {
        try {
            return exerciseDAO.findById(exerciseId);
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz yüklenirken hata: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * İsme göre egzersiz arar
     */
    public List<Exercise> searchExercisesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllExercises();
        }
        
        try {
            return exerciseDAO.findByName(name.trim());
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz arama hatası: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Zorluk seviyesine göre egzersizleri filtreler
     */
    public List<Exercise> getExercisesByDifficulty(String difficultyLevel) {
        try {
            return exerciseDAO.findByDifficulty(difficultyLevel);
        } catch (SQLException e) {
            System.err.println("❌ Egzersiz filtreleme hatası: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Tüm kas gruplarını getirir
     */
    public List<MuscleGroup> getAllMuscleGroups() {
        try {
            return muscleGroupDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Kas grupları yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Tüm ekipmanları getirir
     */
    public List<Equipment> getAllEquipments() {
        try {
            return equipmentDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Ekipmanlar yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Egzersiz adı validasyonu
     */
    private boolean isValidExerciseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ Egzersiz adı boş olamaz!");
            return false;
        }
        
        if (name.trim().length() < 2) {
            System.out.println("❌ Egzersiz adı en az 2 karakter olmalıdır!");
            return false;
        }
        
        if (name.trim().length() > 100) {
            System.out.println("❌ Egzersiz adı en fazla 100 karakter olabilir!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Zorluk seviyesi validasyonu
     */
    private boolean isValidDifficultyLevel(String difficultyLevel) {
        if (difficultyLevel == null || difficultyLevel.trim().isEmpty()) {
            System.out.println("❌ Zorluk seviyesi seçilmelidir!");
            return false;
        }
        
        String[] validLevels = {"kolay", "orta", "zor"};
        for (String level : validLevels) {
            if (level.equals(difficultyLevel)) {
                return true;
            }
        }
        
        System.out.println("❌ Geçerli zorluk seviyeleri: kolay, orta, zor");
        return false;
    }
    
    /**
     * Kas grubu oluşturur (admin yetkisi gerekli)
     */
    public boolean createMuscleGroup(String name, String description) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ Kas grubu adı boş olamaz!");
            return false;
        }
        
        try {
            MuscleGroup muscleGroup = new MuscleGroup();
            muscleGroup.setName(name.trim());
            muscleGroup.setDescription(description != null ? description.trim() : "");
            
            MuscleGroup saved = muscleGroupDAO.save(muscleGroup);
            
            if (saved != null) {
                System.out.println("✅ Kas grubu oluşturuldu: " + name);
                return true;
            } else {
                System.out.println("❌ Kas grubu oluşturulamadı!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Kas grubu oluşturma hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ekipman oluşturur (admin yetkisi gerekli)
     */
    public boolean createEquipment(String name, String description) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ Ekipman adı boş olamaz!");
            return false;
        }
        
        try {
            Equipment equipment = new Equipment();
            equipment.setName(name.trim());
            equipment.setDescription(description != null ? description.trim() : "");
            
            Equipment saved = equipmentDAO.save(equipment);
            
            if (saved != null) {
                System.out.println("✅ Ekipman oluşturuldu: " + name);
                return true;
            } else {
                System.out.println("❌ Ekipman oluşturulamadı!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Ekipman oluşturma hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kas grubuna göre egzersizleri getirir
     */
    public List<Exercise> getExercisesByMuscleGroup(int muscleGroupId) {
        try {
            return exerciseDAO.findByMuscleGroup(muscleGroupId);
        } catch (SQLException e) {
            System.err.println("❌ Kas grubuna göre egzersiz arama hatası: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Ekipmana göre egzersizleri getirir
     */
    public List<Exercise> getExercisesByEquipment(int equipmentId) {
        try {
            return exerciseDAO.findByEquipment(equipmentId);
        } catch (SQLException e) {
            System.err.println("❌ Ekipmana göre egzersiz arama hatası: " + e.getMessage());
            return new ArrayList<>();
        }
    }
} 