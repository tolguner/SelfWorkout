package com.example.selfworkout.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UserWorkouts tablosunu temsil eden model sınıfı
 */
public class UserWorkout {
    private int id;
    private int userId;
    private LocalDate workoutDate;
    private Integer routineId; // nullable
    private Integer exerciseId; // nullable - tek egzersiz antrenmanları için
    private String workoutType; // 'daily' veya 'routine'
    private String notes;
    private String status;
    private Integer durationMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Antrenman verileri (tek egzersiz için)
    private Integer sets;
    private Integer reps;
    private Double weight;
    
    // İlişkili objeler
    private User user;
    private ExerciseRoutine routine;
    private List<UserWorkoutExercise> workoutExercises;
    
    // Constructors
    public UserWorkout() {}
    
    public UserWorkout(int userId, LocalDate workoutDate, String workoutType) {
        this.userId = userId;
        this.workoutDate = workoutDate;
        this.workoutType = workoutType;
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
    
    public Integer getRoutineId() {
        return routineId;
    }
    
    public void setRoutineId(Integer routineId) {
        this.routineId = routineId;
    }
    
    public Integer getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public String getWorkoutType() {
        return workoutType;
    }
    
    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
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
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public ExerciseRoutine getRoutine() {
        return routine;
    }
    
    public void setRoutine(ExerciseRoutine routine) {
        this.routine = routine;
    }
    
    public List<UserWorkoutExercise> getWorkoutExercises() {
        return workoutExercises;
    }
    
    public void setWorkoutExercises(List<UserWorkoutExercise> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }
    
    // Utility methods
    public boolean isRoutineWorkout() {
        return "routine".equals(workoutType);
    }
    
    public boolean isDailyWorkout() {
        return "daily".equals(workoutType);
    }
    
    public int getExerciseCount() {
        return workoutExercises != null ? workoutExercises.size() : 0;
    }
    
    public String getWorkoutTitle() {
        if (isRoutineWorkout() && routine != null) {
            return routine.getName();
        }
        return "Günlük Antrenman - " + workoutDate;
    }
    
    @Override
    public String toString() {
        return getWorkoutTitle() + " (" + workoutDate + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserWorkout that = (UserWorkout) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 