package com.example.selfworkout.model;

import java.time.LocalDateTime;

/**
 * Antrenman modeli - Aktif antrenmanları temsil eder
 */
public class Workout {
    private int id;
    private int userId;
    private int exerciseId;
    private String exerciseName;
    private int plannedSets;
    private int plannedReps;
    private double weight;
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // ACTIVE, COMPLETED, CANCELLED
    private int completedSets;
    private int completedReps;

    // Constructors
    public Workout() {}

    public Workout(int userId, int exerciseId, String exerciseName, int plannedSets, int plannedReps, double weight) {
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.plannedSets = plannedSets;
        this.plannedReps = plannedReps;
        this.weight = weight;
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";
        this.completedSets = 0;
        this.completedReps = 0;
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

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getPlannedSets() {
        return plannedSets;
    }

    public void setPlannedSets(int plannedSets) {
        this.plannedSets = plannedSets;
    }

    public int getPlannedReps() {
        return plannedReps;
    }

    public void setPlannedReps(int plannedReps) {
        this.plannedReps = plannedReps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompletedSets() {
        return completedSets;
    }

    public void setCompletedSets(int completedSets) {
        this.completedSets = completedSets;
    }

    public int getCompletedReps() {
        return completedReps;
    }

    public void setCompletedReps(int completedReps) {
        this.completedReps = completedReps;
    }

    // Utility methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public int getDurationMinutes() {
        if (startTime == null) return 0;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return (int) java.time.Duration.between(startTime, end).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("Workout{id=%d, exercise='%s', sets=%d/%d, reps=%d/%d, weight=%.1f, status='%s'}",
                id, exerciseName, completedSets, plannedSets, completedReps, plannedReps, weight, status);
    }
} 