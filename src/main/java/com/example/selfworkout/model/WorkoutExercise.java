package com.example.selfworkout.model;

import java.math.BigDecimal;

/**
 * WorkoutExercises tablosunu temsil eden model sınıfı
 * Günlük antrenman egzersizlerini yönetir
 */
public class WorkoutExercise {
    private int id;
    private int dailyWorkoutId;
    private int exerciseId;
    private int exerciseOrder;
    private int setCount;
    private String repsPerSet;
    private String weightPerSet;
    private String notes;
    
    // İlişkili objeler
    private DailyWorkout dailyWorkout;
    private Exercise exercise;
    
    // Constructors
    public WorkoutExercise() {}
    
    public WorkoutExercise(int dailyWorkoutId, int exerciseId, int setCount) {
        this.dailyWorkoutId = dailyWorkoutId;
        this.exerciseId = exerciseId;
        this.setCount = setCount;
        this.exerciseOrder = 1;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getDailyWorkoutId() {
        return dailyWorkoutId;
    }
    
    public void setDailyWorkoutId(int dailyWorkoutId) {
        this.dailyWorkoutId = dailyWorkoutId;
    }
    
    public int getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public int getExerciseOrder() {
        return exerciseOrder;
    }
    
    public void setExerciseOrder(int exerciseOrder) {
        this.exerciseOrder = exerciseOrder;
    }
    
    public int getSetCount() {
        return setCount;
    }
    
    public void setSetCount(int setCount) {
        this.setCount = setCount;
    }
    
    public String getRepsPerSet() {
        return repsPerSet;
    }
    
    public void setRepsPerSet(String repsPerSet) {
        this.repsPerSet = repsPerSet;
    }
    
    public String getWeightPerSet() {
        return weightPerSet;
    }
    
    public void setWeightPerSet(String weightPerSet) {
        this.weightPerSet = weightPerSet;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public DailyWorkout getDailyWorkout() {
        return dailyWorkout;
    }
    
    public void setDailyWorkout(DailyWorkout dailyWorkout) {
        this.dailyWorkout = dailyWorkout;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    // Utility methods
    public BigDecimal getTotalVolume() {
        if (weightPerSet != null) {
            BigDecimal weight = new BigDecimal(weightPerSet);
            return weight.multiply(BigDecimal.valueOf(setCount));
        }
        return BigDecimal.ZERO;
    }
    
    public String getFormattedWeight() {
        if (weightPerSet != null) {
            return weightPerSet + " kg";
        }
        return "Vücut ağırlığı";
    }
    
    @Override
    public String toString() {
        return exercise != null ? exercise.getName() + " (" + setCount + " set)" : "Antrenman Egzersizi";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkoutExercise that = (WorkoutExercise) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 