package com.example.selfworkout.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DailyWorkouts tablosunu temsil eden model sınıfı
 * Günlük antrenman planlarını yönetir
 */
public class DailyWorkout {
    private int id;
    private int userId;
    private LocalDate workoutDate;
    private int totalDuration; // dakika cinsinden toplam süre
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    
    // İlişkili objeler
    private User user;
    private List<WorkoutExercise> workoutExercises; // Bu tabloyla ilişkili egzersizler
    
    // Constructors
    public DailyWorkout() {}
    
    public DailyWorkout(int userId, LocalDate workoutDate) {
        this.userId = userId;
        this.workoutDate = workoutDate;
        this.status = "planned";
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
    
    public LocalDate getWorkoutDate() {
        return workoutDate;
    }
    
    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }
    
    public int getTotalDuration() {
        return totalDuration;
    }
    
    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public List<WorkoutExercise> getWorkoutExercises() {
        return workoutExercises;
    }
    
    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }
    
    // Utility methods
    public boolean isToday() {
        return workoutDate.equals(LocalDate.now());
    }
    
    public String getFormattedDate() {
        return workoutDate.toString();
    }
    
    @Override
    public String toString() {
        if (user != null) {
            return user.getFullName() + " - " + workoutDate.toString();
        }
        return "User #" + userId + " - " + workoutDate.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DailyWorkout that = (DailyWorkout) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 