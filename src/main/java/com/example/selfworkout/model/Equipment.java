package com.example.selfworkout.model;

/**
 * Equipments tablosunu temsil eden model sınıfı
 */
public class Equipment {
    private int id;
    private String name;
    private String description;
    
    // Constructors
    public Equipment() {}
    
    public Equipment(String name) {
        this.name = name;
    }
    
    public Equipment(int id, String name) {
        this.id = id;
        this.name = name;
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
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Equipment equipment = (Equipment) obj;
        return id == equipment.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 