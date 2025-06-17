package com.example.selfworkout.service;

import com.example.selfworkout.dao.FavoriteExerciseDAO;
import com.example.selfworkout.dao.ExerciseDAO;
import com.example.selfworkout.model.FavoriteExercise;
import com.example.selfworkout.model.Exercise;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FavoriteExercise için Business Logic sınıfı
 * Favori egzersiz yönetimi işlemlerini koordine eder
 */
public class FavoriteExerciseService {
    
    private final FavoriteExerciseDAO favoriteExerciseDAO;
    private final ExerciseDAO exerciseDAO;
    
    public FavoriteExerciseService() {
        this.favoriteExerciseDAO = new FavoriteExerciseDAO();
        this.exerciseDAO = new ExerciseDAO();
    }
    
    /**
     * Egzersizi favorilere ekler
     */
    public FavoriteExercise addToFavorites(int userId, int exerciseId) throws SQLException {
        // Validasyon
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (exerciseId <= 0) {
            throw new IllegalArgumentException("Geçerli bir egzersiz ID'si gerekli!");
        }
        
        // Egzersiz var mı kontrol et
        Exercise exercise = exerciseDAO.findById(exerciseId);
        if (exercise == null) {
            throw new IllegalArgumentException("Egzersiz bulunamadı!");
        }
        
        // Zaten favorilerde mi kontrol et
        if (favoriteExerciseDAO.exists(userId, exerciseId)) {
            throw new IllegalArgumentException("Bu egzersiz zaten favorilerinizde!");
        }
        
        // Favorilere ekle
        FavoriteExercise favoriteExercise = new FavoriteExercise();
        favoriteExercise.setUserId(userId);
        favoriteExercise.setExerciseId(exerciseId);
        
        return favoriteExerciseDAO.save(favoriteExercise);
    }
    
    /**
     * Egzersizi favorilerden kaldırır
     */
    public boolean removeFromFavorites(int userId, int exerciseId) throws SQLException {
        // Validasyon
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (exerciseId <= 0) {
            throw new IllegalArgumentException("Geçerli bir egzersiz ID'si gerekli!");
        }
        
        // Favorilerde var mı kontrol et
        if (!favoriteExerciseDAO.exists(userId, exerciseId)) {
            throw new IllegalArgumentException("Bu egzersiz favorilerinizde değil!");
        }
        
        return favoriteExerciseDAO.deleteByUserAndExercise(userId, exerciseId);
    }
    
    /**
     * Favori egzersizi ID ile siler
     */
    public boolean removeFavoriteById(int favoriteId) throws SQLException {
        if (favoriteId <= 0) {
            throw new IllegalArgumentException("Geçerli bir favori ID'si gerekli!");
        }
        
        return favoriteExerciseDAO.delete(favoriteId);
    }
    
    /**
     * Kullanıcının tüm favori egzersizlerini getirir
     */
    public List<FavoriteExercise> getUserFavorites(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return favoriteExerciseDAO.findByUserId(userId);
    }
    
    /**
     * Kullanıcının favori egzersizlerini Exercise nesneleri olarak getirir
     */
    public List<Exercise> getUserFavoriteExercises(int userId) throws SQLException {
        List<FavoriteExercise> favorites = getUserFavorites(userId);
        
        return favorites.stream()
                .map(favorite -> {
                    try {
                        return exerciseDAO.findById(favorite.getExerciseId());
                    } catch (SQLException e) {
                        System.err.println("Favori egzersiz yüklenirken hata: " + e.getMessage());
                        return null;
                    }
                })
                .filter(exercise -> exercise != null)
                .collect(Collectors.toList());
    }
    
    /**
     * Egzersizin favorilerde olup olmadığını kontrol eder
     */
    public boolean isFavorite(int userId, int exerciseId) throws SQLException {
        if (userId <= 0 || exerciseId <= 0) {
            return false;
        }
        
        return favoriteExerciseDAO.exists(userId, exerciseId);
    }
    
    /**
     * Kullanıcının favori egzersiz sayısını getirir
     */
    public int getFavoriteCount(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        return favoriteExerciseDAO.findByUserId(userId).size();
    }
    
    /**
     * Favori egzersizleri kas grubuna göre filtreler
     */
    public List<Exercise> getFavoritesByMuscleGroup(int userId, String muscleGroupName) throws SQLException {
        List<Exercise> favoriteExercises = getUserFavoriteExercises(userId);
        
        if (muscleGroupName == null || muscleGroupName.trim().isEmpty()) {
            return favoriteExercises;
        }
        
        return favoriteExercises.stream()
                .filter(exercise -> {
                    // Bu basit bir implementasyon - gerçek uygulamada 
                    // ExerciseMuscle tablosundan kontrol edilmeli
                    return exercise.getDescription() != null && 
                           exercise.getDescription().toLowerCase().contains(muscleGroupName.toLowerCase());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Favori egzersizleri zorluk seviyesine göre filtreler
     */
    public List<Exercise> getFavoritesByDifficulty(int userId, String difficulty) throws SQLException {
        List<Exercise> favoriteExercises = getUserFavoriteExercises(userId);
        
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return favoriteExercises;
        }
        
        return favoriteExercises.stream()
                .filter(exercise -> difficulty.equalsIgnoreCase(exercise.getDifficultyLevel()))
                .collect(Collectors.toList());
    }
    
    /**
     * Favori egzersizleri toggle eder (varsa kaldırır, yoksa ekler)
     */
    public boolean toggleFavorite(int userId, int exerciseId) throws SQLException {
        if (isFavorite(userId, exerciseId)) {
            removeFromFavorites(userId, exerciseId);
            System.out.println("✅ Egzersiz favorilerden kaldırıldı.");
            return false; // Artık favori değil
        } else {
            addToFavorites(userId, exerciseId);
            System.out.println("✅ Egzersiz favorilere eklendi.");
            return true; // Artık favori
        }
    }
    
    /**
     * Kullanıcının en popüler favori egzersizlerini getirir
     * (Bu basit bir implementasyon - gerçek uygulamada kullanım istatistikleri kullanılabilir)
     */
    public List<Exercise> getTopFavoriteExercises(int userId, int limit) throws SQLException {
        List<Exercise> favorites = getUserFavoriteExercises(userId);
        
        // Basit sıralama - alfabetik
        return favorites.stream()
                .sorted((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Favori egzersizleri arama
     */
    public List<Exercise> searchFavorites(int userId, String searchTerm) throws SQLException {
        List<Exercise> favorites = getUserFavoriteExercises(userId);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return favorites;
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        return favorites.stream()
                .filter(exercise -> 
                    exercise.getName().toLowerCase().contains(lowerSearchTerm) ||
                    (exercise.getDescription() != null && 
                     exercise.getDescription().toLowerCase().contains(lowerSearchTerm))
                )
                .collect(Collectors.toList());
    }
} 