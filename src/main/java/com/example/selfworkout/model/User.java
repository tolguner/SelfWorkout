package com.example.selfworkout.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Users tablosunu temsil eden model sınıfı
 */
public class User {
    private int id;
    private int roleId;
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthdate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // İlişkili objeler (JOIN'ler için)
    private Role role;
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getRoleId() {
        return roleId;
    }
    
    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public LocalDate getBirthdate() {
        return birthdate;
    }
    
    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    // Utility methods
    public String getFullName() {
        if (name != null && surname != null) {
            return name + " " + surname;
        } else if (name != null) {
            return name;
        } else if (surname != null) {
            return surname;
        }
        return username;
    }
    
    public boolean isAdmin() {
        return role != null && "admin".equals(role.getRoleName());
    }
    
    public boolean isSporcu() {
        return role != null && "sporcu".equals(role.getRoleName());
    }
    
    public String getRoleName() {
        return role != null ? role.getRoleName() : null;
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + username + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 