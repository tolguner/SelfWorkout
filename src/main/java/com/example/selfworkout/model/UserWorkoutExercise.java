package com.example.selfworkout.model;

import java.math.BigDecimal;

/**
 * UserWorkoutExercises tablosunu temsil eden model sınıfı
 */
public class UserWorkoutExercise {
    private int id;
    private int userWorkoutId;
    private int exerciseId;
    private int setNumber;
    private int actualReps;
    private BigDecimal actualWeight;
    private String notes;
    
    // İlişkili objeler
    private UserWorkout userWorkout;
    private Exercise exercise;
    
    // Constructors
    public UserWorkoutExercise() {}
    
    public UserWorkoutExercise(int userWorkoutId, int exerciseId, int setNumber, int actualReps, BigDecimal actualWeight) {
        this.userWorkoutId = userWorkoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.actualReps = actualReps;
        this.actualWeight = actualWeight;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserWorkoutId() {
        return userWorkoutId;
    }
    
    public void setUserWorkoutId(int userWorkoutId) {
        this.userWorkoutId = userWorkoutId;
    }
    
    public int getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public int getSetNumber() {
        return setNumber;
    }
    
    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }
    
    public int getActualReps() {
        return actualReps;
    }
    
    public void setActualReps(int actualReps) {
        this.actualReps = actualReps;
    }
    
    public BigDecimal getActualWeight() {
        return actualWeight;
    }
    
    public void setActualWeight(BigDecimal actualWeight) {
        this.actualWeight = actualWeight;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public UserWorkout getUserWorkout() {
        return userWorkout;
    }
    
    public void setUserWorkout(UserWorkout userWorkout) {
        this.userWorkout = userWorkout;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    // Utility methods
    public String getSetInfo() {
        return String.format("Set %d: %d reps @ %.1f kg", setNumber, actualReps, actualWeight);
    }
    
    public double getVolume() {
        return actualWeight.doubleValue() * actualReps;
    }
    
    public String getFormattedWeight() {
        if (actualWeight != null && !actualWeight.equals(BigDecimal.ZERO)) {
            return String.format("%.1f kg", actualWeight.doubleValue());
        }
        return "Vücut ağırlığı";
    }
    
    @Override
    public String toString() {
        String exerciseName = (exercise != null) ? exercise.getName() : "Egzersiz #" + exerciseId;
        return exerciseName + " - " + getSetInfo();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserWorkoutExercise that = (UserWorkoutExercise) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 