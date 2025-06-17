package com.example.selfworkout.model;

import java.util.List;

/**
 * Exercises tablosunu temsil eden model sınıfı
 */
public class Exercise {
    private int id;
    private String name;
    private String description;
    private String difficultyLevel;
    private String instructions;
    private int createdBy;
    
    // İlişkili objeler (JOIN'ler için)
    private User creator;
    private List<MuscleGroup> muscleGroups;
    private List<Equipment> equipments;
    private List<String> tags;
    
    // Constructors
    public Exercise() {}
    
    public Exercise(String name, String description, int createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public List<MuscleGroup> getMuscleGroups() {
        return muscleGroups;
    }
    
    public void setMuscleGroups(List<MuscleGroup> muscleGroups) {
        this.muscleGroups = muscleGroups;
    }
    
    public List<Equipment> getEquipments() {
        return equipments;
    }
    
    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    // Utility methods
    public String getMuscleGroupsAsString() {
        if (muscleGroups == null || muscleGroups.isEmpty()) {
            return "-";
        }
        return muscleGroups.stream()
                .map(MuscleGroup::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }
    
    /**
     * Ana kas grubunu döndürür (ilk kas grubu)
     */
    public String getMuscleGroup() {
        if (muscleGroups == null || muscleGroups.isEmpty()) {
            return "Genel";
        }
        return muscleGroups.get(0).getName();
    }
    
    /**
     * Zorluk seviyesini döndürür
     */
    public String getDifficulty() {
        return difficultyLevel != null ? difficultyLevel : "Orta";
    }
    
    public String getEquipmentsAsString() {
        if (equipments == null || equipments.isEmpty()) {
            return "-";
        }
        return equipments.stream()
                .map(Equipment::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Exercise exercise = (Exercise) obj;
        return id == exercise.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 