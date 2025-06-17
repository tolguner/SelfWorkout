package com.example.selfworkout.model;

import java.time.LocalDateTime;

/**
 * Logs tablosunu temsil eden model sınıfı
 * Sistem loglarını ve kullanıcı aktivitelerini yönetir
 */
public class Log {
    private int id;
    private Integer userId; // Nullable - sistem logları için null olabilir
    private String action;
    private String description;
    private LocalDateTime timestamp;
    
    // İlişkili objeler
    private User user;
    
    // Constructors
    public Log() {}
    
    public Log(String action, String description) {
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    public Log(Integer userId, String action, String description) {
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Utility methods
    public boolean isUserAction() {
        return userId != null;
    }
    
    public boolean isSystemAction() {
        return userId == null;
    }
    
    public String getFormattedTimestamp() {
        return timestamp.toString();
    }
    
    @Override
    public String toString() {
        String userInfo = (user != null) ? user.getUsername() : 
                         (userId != null) ? "User #" + userId : "System";
        return timestamp + " - " + userInfo + " - " + action + ": " + description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Log log = (Log) obj;
        return id == log.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 