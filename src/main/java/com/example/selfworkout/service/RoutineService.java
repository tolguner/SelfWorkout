package com.example.selfworkout.service;

import com.example.selfworkout.dao.ExerciseRoutineDAO;
import com.example.selfworkout.dao.RoutineExerciseDAO;
import com.example.selfworkout.model.ExerciseRoutine;
import com.example.selfworkout.model.RoutineExercise;

import java.sql.SQLException;
import java.util.List;

/**
 * ExerciseRoutine ve RoutineExercise için Business Logic sınıfı
 * Rutin yönetimi işlemlerini koordine eder
 */
public class RoutineService {
    
    private final ExerciseRoutineDAO exerciseRoutineDAO;
    private final RoutineExerciseDAO routineExerciseDAO;
    
    public RoutineService() {
        this.exerciseRoutineDAO = new ExerciseRoutineDAO();
        this.routineExerciseDAO = new RoutineExerciseDAO();
    }
    
    /**
     * Yeni rutin oluşturur
     */
    public ExerciseRoutine createRoutine(ExerciseRoutine routine) throws SQLException {
        // Validasyon
        if (routine.getName() == null || routine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rutin adı boş olamaz!");
        }
        
        if (routine.getUserId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        // Rutin oluştur
        ExerciseRoutine savedRoutine = exerciseRoutineDAO.save(routine);
        System.out.println("✅ Rutin başarıyla oluşturuldu: " + savedRoutine.getName());
        
        return savedRoutine;
    }
    
    /**
     * Rutini günceller
     */
    public boolean updateRoutine(ExerciseRoutine routine) throws SQLException {
        // Validasyon
        if (routine.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir rutin ID'si gerekli!");
        }
        
        if (routine.getName() == null || routine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rutin adı boş olamaz!");
        }
        
        // Rutin var mı kontrol et
        ExerciseRoutine existingRoutine = exerciseRoutineDAO.findById(routine.getId());
        if (existingRoutine == null) {
            throw new IllegalArgumentException("Güncellenecek rutin bulunamadı!");
        }
        
        return exerciseRoutineDAO.update(routine);
    }
    
    /**
     * Rutini siler (egzersizleri ile birlikte)
     */
    public boolean deleteRoutine(int routineId) throws SQLException {
        // Rutin var mı kontrol et
        ExerciseRoutine routine = exerciseRoutineDAO.findById(routineId);
        if (routine == null) {
            throw new IllegalArgumentException("Silinecek rutin bulunamadı!");
        }
        
        // Önce rutin egzersizlerini sil
        routineExerciseDAO.deleteByRoutineId(routineId);
        
        // Sonra rutini sil
        boolean deleted = exerciseRoutineDAO.delete(routineId);
        
        if (deleted) {
            System.out.println("✅ Rutin ve tüm egzersizleri başarıyla silindi.");
        }
        
        return deleted;
    }
    
    /**
     * ID'ye göre rutin getirir (egzersizleri ile birlikte)
     */
    public ExerciseRoutine getRoutineWithExercises(int routineId) throws SQLException {
        ExerciseRoutine routine = exerciseRoutineDAO.findById(routineId);
        if (routine != null) {
            // Rutin egzersizlerini yükle
            List<RoutineExercise> exercises = routineExerciseDAO.findByRoutineId(routineId);
            routine.setRoutineExercises(exercises);
        }
        return routine;
    }
    
    /**
     * Kullanıcının tüm rutinlerini getirir
     */
    public List<ExerciseRoutine> getUserRoutines(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return exerciseRoutineDAO.findByUserId(userId);
    }
    
    /**
     * Kullanıcının sınırlı sayıda rutinini getirir
     */
    public List<ExerciseRoutine> getUserRoutines(int userId, int limit) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return exerciseRoutineDAO.findByUserId(userId, limit);
    }
    
    /**
     * Kullanıcının popüler rutinlerini getirir
     */
    public List<ExerciseRoutine> getPopularRoutines(int userId, int limit) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return exerciseRoutineDAO.findPopularByUserId(userId, limit);
    }
    
    /**
     * Rutine egzersiz ekler
     */
    public RoutineExercise addExerciseToRoutine(RoutineExercise routineExercise) throws SQLException {
        // Validasyon
        if (routineExercise.getRoutineId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir rutin ID'si gerekli!");
        }
        
        if (routineExercise.getExerciseId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir egzersiz ID'si gerekli!");
        }
        
        // Rutin var mı kontrol et
        ExerciseRoutine routine = exerciseRoutineDAO.findById(routineExercise.getRoutineId());
        if (routine == null) {
            throw new IllegalArgumentException("Rutin bulunamadı!");
        }
        
        return routineExerciseDAO.save(routineExercise);
    }
    
    /**
     * Rutinden egzersiz kaldırır
     */
    public boolean removeExerciseFromRoutine(int routineExerciseId) throws SQLException {
        if (routineExerciseId <= 0) {
            throw new IllegalArgumentException("Geçerli bir rutin egzersiz ID'si gerekli!");
        }
        
        return routineExerciseDAO.delete(routineExerciseId);
    }
    
    /**
     * Rutin egzersizlerini toplu olarak günceller
     */
    public boolean updateRoutineExercises(int routineId, List<RoutineExercise> exercises) throws SQLException {
        // Validasyon
        if (routineId <= 0) {
            throw new IllegalArgumentException("Geçerli bir rutin ID'si gerekli!");
        }
        
        // Rutin var mı kontrol et
        ExerciseRoutine routine = exerciseRoutineDAO.findById(routineId);
        if (routine == null) {
            throw new IllegalArgumentException("Rutin bulunamadı!");
        }
        
        // Önce mevcut egzersizleri sil
        routineExerciseDAO.deleteByRoutineId(routineId);
        
        // Yeni egzersizleri tek tek ekle
        if (exercises != null && !exercises.isEmpty()) {
            for (RoutineExercise exercise : exercises) {
                exercise.setRoutineId(routineId);
                routineExerciseDAO.save(exercise);
            }
        }
        
        System.out.println("✅ Rutin egzersizleri başarıyla güncellendi.");
        return true;
    }
    
    /**
     * Kullanıcının rutin sayısını getirir
     */
    public int getUserRoutineCount(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return exerciseRoutineDAO.countByUserId(userId);
    }
    
    /**
     * İsme göre rutin arar
     */
    public List<ExerciseRoutine> searchRoutinesByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Arama terimi boş olamaz!");
        }
        
        return exerciseRoutineDAO.findByName(name);
    }
    
    /**
     * Rutin kopyalar (yeni kullanıcı için)
     */
    public ExerciseRoutine copyRoutine(int originalRoutineId, int newUserId, String newName) throws SQLException {
        // Orijinal rutini al
        ExerciseRoutine originalRoutine = getRoutineWithExercises(originalRoutineId);
        if (originalRoutine == null) {
            throw new IllegalArgumentException("Kopyalanacak rutin bulunamadı!");
        }
        
        // Yeni rutin oluştur
        ExerciseRoutine newRoutine = new ExerciseRoutine();
        newRoutine.setUserId(newUserId);
        newRoutine.setName(newName != null ? newName : originalRoutine.getName() + " (Kopya)");
        newRoutine.setDescription(originalRoutine.getDescription());
        
        ExerciseRoutine savedRoutine = exerciseRoutineDAO.save(newRoutine);
        
        // Egzersizleri kopyala
        if (originalRoutine.getRoutineExercises() != null) {
            for (RoutineExercise originalExercise : originalRoutine.getRoutineExercises()) {
                RoutineExercise newExercise = new RoutineExercise();
                newExercise.setRoutineId(savedRoutine.getId());
                newExercise.setExerciseId(originalExercise.getExerciseId());
                newExercise.setExerciseOrder(originalExercise.getExerciseOrder());
                newExercise.setSetCount(originalExercise.getSetCount());
                newExercise.setRepsPerSet(originalExercise.getRepsPerSet());
                newExercise.setWeightPerSet(originalExercise.getWeightPerSet());
                
                routineExerciseDAO.save(newExercise);
            }
        }
        
        System.out.println("✅ Rutin başarıyla kopyalandı: " + savedRoutine.getName());
        return savedRoutine;
    }
} 