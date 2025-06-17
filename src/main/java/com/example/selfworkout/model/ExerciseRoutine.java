package com.example.selfworkout.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ExerciseRoutines tablosunu temsil eden model sınıfı
 */
public class ExerciseRoutine {
    private int id;
    private int userId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    
    // İlişkili objeler
    private User user;
    private List<RoutineExercise> routineExercises;
    
    // Constructors
    public ExerciseRoutine() {}
    
    public ExerciseRoutine(String name, int userId) {
        this.name = name;
        this.userId = userId;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public List<RoutineExercise> getRoutineExercises() {
        return routineExercises;
    }
    
    public void setRoutineExercises(List<RoutineExercise> routineExercises) {
        this.routineExercises = routineExercises;
    }
    
    // Utility methods
    public int getExerciseCount() {
        return routineExercises != null ? routineExercises.size() : 0;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExerciseRoutine that = (ExerciseRoutine) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 