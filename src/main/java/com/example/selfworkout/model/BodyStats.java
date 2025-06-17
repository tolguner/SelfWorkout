package com.example.selfworkout.model;

import java.time.LocalDate;

/**
 * BodyStats tablosunu temsil eden model sınıfı
 */
public class BodyStats {
    private int id;
    private int userId;
    private LocalDate recordDate;
    private double height; // cm cinsinden
    private double weight; // kg cinsinden
    private double bodyFat; // vücut yağ oranı (%)
    private double muscleMass; // kas kütlesi (kg)
    private String notes;
    
    // İlişkili objeler
    private User user;
    
    // Constructors
    public BodyStats() {}
    
    public BodyStats(int userId, double height, double weight) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.recordDate = LocalDate.now();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public double getBodyFat() {
        return bodyFat;
    }
    
    public void setBodyFat(double bodyFat) {
        this.bodyFat = bodyFat;
    }
    
    public double getMuscleMass() {
        return muscleMass;
    }
    
    public void setMuscleMass(double muscleMass) {
        this.muscleMass = muscleMass;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Utility methods
    public double calculateBMI() {
        if (height > 0 && weight > 0) {
            double heightM = height / 100.0;
            return Math.round((weight / (heightM * heightM)) * 100.0) / 100.0;
        }
        return 0.0;
    }
    
    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi == 0.0) return "Belirsiz";
        if (bmi < 18.5) return "Zayıf";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Fazla Kilolu";
        return "Obez";
    }
    
    @Override
    public String toString() {
        return String.format("%.1f kg - %.1f cm (BMI: %.1f)", weight, height, calculateBMI());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BodyStats bodyStats = (BodyStats) obj;
        return id == bodyStats.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 