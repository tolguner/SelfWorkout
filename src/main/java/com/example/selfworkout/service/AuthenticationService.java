package com.example.selfworkout.service;

import com.example.selfworkout.dao.UserDAO;
import com.example.selfworkout.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Authentication ve session yönetimi için Service sınıfı
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private User currentUser; // Aktif kullanıcı (session)

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Kullanıcı girişi yapar
     */
    public User login(String username, String password) {
        try {
            User user = userDAO.authenticate(username, password);

            if (user != null) {
                this.currentUser = user;
                System.out.println("🎯 Hoş geldiniz: " + user.getFullName());
                logUserActivity("Giriş yapıldı");
                return user;
            } else {
                System.out.println("❌ Geçersiz kullanıcı adı veya şifre!");
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Giriş sırasında hata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kullanıcı çıkışı yapar
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 Güle güle: " + currentUser.getFullName());
            logUserActivity("Çıkış yapıldı");
            this.currentUser = null;
        }
    }

    /**
     * Kullanıcının giriş yapıp yapmadığını kontrol eder
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Aktif kullanıcıyı döndürür
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Aktif kullanıcıyı ayarlar (manuel login için)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("🎯 Manuel giriş: " + user.getFullName());
            logUserActivity("Manuel giriş yapıldı");
        }
    }

    /**
     * Aktif kullanıcının ID'sini döndürür
     */
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Aktif kullanıcının admin olup olmadığını kontrol eder
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Aktif kullanıcının sporcu olup olmadığını kontrol eder
     */
    public boolean isCurrentUserSporcu() {
        return currentUser != null && currentUser.isSporcu();
    }

    /**
     * Şifre değiştirme
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!isLoggedIn()) {
            System.out.println("❌ Şifre değiştirmek için giriş yapmanız gerekiyor!");
            return false;
        }

        // Eski şifreyi kontrol et
        if (!currentUser.getPassword().equals(oldPassword)) {
            System.out.println("❌ Mevcut şifre yanlış!");
            return false;
        }

        // Yeni şifre validasyonu
        if (!isValidPassword(newPassword)) { // ValidationService'den çağrılmalı veya burada tanımlı olmalı
            return false;
        }

        try {
            boolean success = userDAO.updatePassword(currentUser.getId(), newPassword);

            if (success) {
                currentUser.setPassword(newPassword);
                System.out.println("✅ Şifre başarıyla değiştirildi!");
                logUserActivity("Şifre değiştirildi");
                return true;
            } else {
                System.out.println("❌ Şifre değiştirilemedi!");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Şifre değiştirme sırasında hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcı kaydı yapar
     */
    public boolean register(String username, String email, String password, String name, String surname, int roleId) {
        // ValidationService kullanarak doğrulama yapın.
        // Bu örnekte basitleştirilmiş bir isValidUsername/Email/Password kullanılıyor.

        // Validation
        if (!isValidUsername(username)) {
            return false;
        }

        if (!isValidEmail(email)) {
            return false;
        }

        if (!isValidPassword(password)) {
            return false;
        }

        try {
            // Yeni kullanıcı oluştur
            User newUser = new User(username, email, password);
            newUser.setRoleId(roleId);
            newUser.setName(name);
            newUser.setSurname(surname);

            User savedUser = userDAO.save(newUser);

            if (savedUser != null) {
                System.out.println("✅ Kullanıcı başarıyla kaydedildi: " + username);
                return true;
            } else {
                System.out.println("❌ Kullanıcı kaydedilemedi!");
                return false;
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("kullanıcı adı")) {
                System.out.println("❌ Bu kullanıcı adı zaten kullanılıyor!");
            } else if (e.getMessage().contains("email")) {
                System.out.println("❌ Bu email adresi zaten kullanılıyor!");
            } else {
                System.err.println("❌ Kayıt sırasında hata: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Session'ın geçerli olup olmadığını kontrol eder
     */
    public boolean isSessionValid() {
        if (!isLoggedIn()) {
            return false;
        }

        // Kullanıcının hala var olup olmadığını kontrol et
        try {
            User user = userDAO.findById(currentUser.getId());
            if (user == null) {
                logout(); // Kullanıcı silinmişse çıkış yap
                return false;
            }

            // Güncel kullanıcı bilgilerini yükle
            this.currentUser = user;
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Session kontrol hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcı aktivitesini loglar
     */
    private void logUserActivity(String activity) {
        // Normalde LogService kullanılmalı
        // ServiceManager.getInstance().getLogService().createLog("USER_ACTIVITY", activity, getCurrentUserId());
        if (currentUser != null) {
            System.out.println("📝 [" + LocalDateTime.now() + "] " +
                    currentUser.getUsername() + " - " + activity);
        } else {
            System.out.println("📝 [" + LocalDateTime.now() + "] SYSTEM - " + activity);
        }
    }

    /**
     * Kullanıcı adı validasyonu (basit)
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Kullanıcı adı boş olamaz!");
            return false;
        }

        if (username.length() < 3) {
            System.out.println("❌ Kullanıcı adı en az 3 karakter olmalıdır!");
            return false;
        }

        if (username.length() > 50) {
            System.out.println("❌ Kullanıcı adı en fazla 50 karakter olabilir!");
            return false;
        }

        // Alfanumerik ve underscore kontrolü
        // DÜZELTİLDİ: Regex sonu $ ile bitmeli
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            System.out.println("❌ Kullanıcı adı sadece harf, rakam ve _ içerebilir!");
            return false;
        }

        return true;
    }

    /**
     * Email validasyonu (basit)
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ Email adresi boş olamaz!");
            return false;
        }

        // Basit email regex
        // DÜZELTİLDİ: Regex sonu $ ile bitmeli
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            System.out.println("❌ Geçerli bir email adresi giriniz!");
            return false;
        }

        return true;
    }

    /**
     * Şifre validasyonu (basit)
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("❌ Şifre boş olamaz!");
            return false;
        }

        if (password.length() < 6) {
            System.out.println("❌ Şifre en az 6 karakter olmalıdır!");
            return false;
        }

        if (password.length() > 100) {
            System.out.println("❌ Şifre çok uzun!");
            return false;
        }

        return true;
    }

    /**
     * Güvenlik kontrolü - admin yetkisi gerektirir
     */
    public boolean requireAdmin() {
        if (!isLoggedIn()) {
            System.out.println("❌ Bu işlem için giriş yapmanız gerekiyor!");
            return false;
        }

        if (!isCurrentUserAdmin()) {
            System.out.println("❌ Bu işlem için admin yetkisi gerekiyor!");
            return false;
        }

        return true;
    }

    /**
     * Güvenlik kontrolü - giriş yapmış kullanıcı gerektirir
     */
    public boolean requireLogin() {
        if (!isLoggedIn()) {
            System.out.println("❌ Bu işlem için giriş yapmanız gerekiyor!");
            return false;
        }

        return true;
    }
}