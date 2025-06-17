package com.example.selfworkout.model;

/**
 * ExerciseMuscles tablosunu temsil eden model sınıfı
 * Egzersiz ve kas grubu arasındaki many-to-many ilişkiyi yönetir
 */
public class ExerciseMuscle {
    private int id;
    private int exerciseId;
    private int muscleId;
    
    // İlişkili objeler
    private Exercise exercise;
    private MuscleGroup muscleGroup;
    
    // Constructors
    public ExerciseMuscle() {}
    
    public ExerciseMuscle(int exerciseId, int muscleId) {
        this.exerciseId = exerciseId;
        this.muscleId = muscleId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public int getMuscleId() {
        return muscleId;
    }
    
    public void setMuscleId(int muscleId) {
        this.muscleId = muscleId;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    public MuscleGroup getMuscleGroup() {
        return muscleGroup;
    }
    
    public void setMuscleGroup(MuscleGroup muscleGroup) {
        this.muscleGroup = muscleGroup;
    }
    
    @Override
    public String toString() {
        String exerciseName = exercise != null ? exercise.getName() : "Egzersiz #" + exerciseId;
        String muscleName = muscleGroup != null ? muscleGroup.getName() : "Kas #" + muscleId;
        return exerciseName + " - " + muscleName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExerciseMuscle that = (ExerciseMuscle) obj;
        return exerciseId == that.exerciseId && muscleId == that.muscleId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(exerciseId) + Integer.hashCode(muscleId);
    }
} 