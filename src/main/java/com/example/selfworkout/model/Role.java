package com.example.selfworkout.model;

/**
 * Roles tablosunu temsil eden model sınıfı
 */
public class Role {
    private int id;
    private String roleName;
    private String description;
    
    // Constructors
    public Role() {}
    
    public Role(String roleName) {
        this.roleName = roleName;
    }
    
    public Role(int id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return roleName; // ComboBox'larda gösterim için
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return id == role.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 