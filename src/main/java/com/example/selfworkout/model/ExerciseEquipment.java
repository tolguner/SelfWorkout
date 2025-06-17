package com.example.selfworkout.model;

/**
 * ExerciseEquipments tablosunu temsil eden model sınıfı
 * Egzersiz ve ekipman arasındaki many-to-many ilişkiyi yönetir
 */
public class ExerciseEquipment {
    private int id;
    private int exerciseId;
    private int equipmentId;
    
    // İlişkili objeler
    private Exercise exercise;
    private Equipment equipment;
    
    // Constructors
    public ExerciseEquipment() {}
    
    public ExerciseEquipment(int exerciseId, int equipmentId) {
        this.exerciseId = exerciseId;
        this.equipmentId = equipmentId;
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
    
    public int getEquipmentId() {
        return equipmentId;
    }
    
    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    public Equipment getEquipment() {
        return equipment;
    }
    
    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }
    
    @Override
    public String toString() {
        String exerciseName = exercise != null ? exercise.getName() : "Egzersiz #" + exerciseId;
        String equipmentName = equipment != null ? equipment.getName() : "Ekipman #" + equipmentId;
        return exerciseName + " - " + equipmentName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExerciseEquipment that = (ExerciseEquipment) obj;
        return exerciseId == that.exerciseId && equipmentId == that.equipmentId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(exerciseId) + Integer.hashCode(equipmentId);
    }
} 