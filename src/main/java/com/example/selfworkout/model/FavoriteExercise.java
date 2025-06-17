package com.example.selfworkout.model;

import java.time.LocalDateTime;

/**
 * FavoriteExercises tablosunu temsil eden model sınıfı
 * Kullanıcıların favori egzersizlerini yönetir
 */
public class FavoriteExercise {
    private int id;
    private int userId;
    private int exerciseId;
    private LocalDateTime createdAt;
    
    // İlişkili objeler
    private User user;
    private Exercise exercise;
    
    // Constructors
    public FavoriteExercise() {}
    
    public FavoriteExercise(int userId, int exerciseId) {
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    // Utility methods
    public String getFormattedCreatedAt() {
        return createdAt.toString();
    }
    
    public boolean isRecentlyAdded() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    @Override
    public String toString() {
        String userInfo = (user != null) ? user.getUsername() : "User #" + userId;
        String exerciseInfo = (exercise != null) ? exercise.getName() : "Exercise #" + exerciseId;
        return userInfo + " ⭐ " + exerciseInfo;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoriteExercise that = (FavoriteExercise) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 