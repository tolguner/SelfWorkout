package com.example.selfworkout.model;

/**
 * RoutineExercises tablosunu temsil eden model sınıfı
 */
public class RoutineExercise {
    private int id;
    private int routineId;
    private int exerciseId;
    private int exerciseOrder;
    private int setCount;
    private String repsPerSet;
    private String weightPerSet;
    
    // İlişkili objeler
    private ExerciseRoutine routine;
    private Exercise exercise;
    
    // Constructors
    public RoutineExercise() {}
    
    public RoutineExercise(int routineId, int exerciseId, int setCount) {
        this.routineId = routineId;
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
    
    public int getRoutineId() {
        return routineId;
    }
    
    public void setRoutineId(int routineId) {
        this.routineId = routineId;
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
    
    public ExerciseRoutine getRoutine() {
        return routine;
    }
    
    public void setRoutine(ExerciseRoutine routine) {
        this.routine = routine;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    @Override
    public String toString() {
        return exercise != null ? exercise.getName() + " (" + setCount + " set)" : "Rutin Egzersizi";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoutineExercise that = (RoutineExercise) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 