package com.example.selfworkout.model;

/**
 * ExerciseTags tablosunu temsil eden model sınıfı
 * Egzersizlere tag/etiket ekleme özelliğini yönetir
 */
public class ExerciseTag {
    private int id;
    private int exerciseId;
    private String tag;
    
    // İlişkili objeler
    private Exercise exercise;
    
    // Constructors
    public ExerciseTag() {}
    
    public ExerciseTag(int exerciseId, String tag) {
        this.exerciseId = exerciseId;
        this.tag = tag;
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
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    @Override
    public String toString() {
        if (exercise != null) {
            return exercise.getName() + " -> " + tag;
        }
        return "Exercise #" + exerciseId + " -> " + tag;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExerciseTag that = (ExerciseTag) obj;
        return exerciseId == that.exerciseId && tag.equals(that.tag);
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(exerciseId) + tag.hashCode();
    }
} 