package com.example.selfworkout.service;

import com.example.selfworkout.dao.UserDAO;
import com.example.selfworkout.dao.RoleDAO;
import com.example.selfworkout.model.User;
import com.example.selfworkout.model.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * User yönetimi için Service sınıfı
 */
public class UserService {
    
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final AuthenticationService authService;
    
    public UserService(AuthenticationService authService) {
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
        this.authService = authService;
    }
    
    /**
     * Kullanıcı profil bilgilerini günceller
     */
    public boolean updateProfile(String name, String surname, String email, LocalDate birthdate) {
        if (!authService.requireLogin()) {
            return false;
        }
        
        try {
            User currentUser = authService.getCurrentUser();
            
            // Email değişmişse kontrol et
            if (!currentUser.getEmail().equals(email)) {
                if (userDAO.isEmailExists(email, currentUser.getId())) {
                    System.out.println("❌ Bu email adresi zaten kullanılıyor!");
                    return false;
                }
            }
            
            // Güncelle
            currentUser.setName(name != null ? name.trim() : "");
            currentUser.setSurname(surname != null ? surname.trim() : "");
            currentUser.setEmail(email.trim());
            currentUser.setBirthdate(birthdate);
            
            boolean success = userDAO.update(currentUser);
            
            if (success) {
                System.out.println("✅ Profil başarıyla güncellendi!");
                return true;
            } else {
                System.out.println("❌ Profil güncellenemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Profil güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kullanıcının kendi profilini getirir
     */
    public User getUserProfile() {
        if (!authService.requireLogin()) {
            return null;
        }
        
        try {
            // Güncel bilgileri veritabanından al
            return userDAO.findById(authService.getCurrentUserId());
        } catch (SQLException e) {
            System.err.println("❌ Profil yükleme hatası: " + e.getMessage());
            return authService.getCurrentUser(); // Fallback
        }
    }
    
    /**
     * Tüm kullanıcıları getirir (admin için)
     */
    public List<User> getAllUsers() {
        if (!authService.requireAdmin()) {
            return new ArrayList<>();
        }
        
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcılar yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * ID'ye göre kullanıcı getirir
     */
    public User getUserById(int userId) {
        try {
            return userDAO.findById(userId);
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı ID ile arama hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Kullanıcı rolünü günceller (admin için)
     */
    public boolean updateUserRole(int userId, int newRoleId) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        try {
            // Kullanıcıyı bul
            User user = userDAO.findById(userId);
            if (user == null) {
                System.out.println("❌ Kullanıcı bulunamadı!");
                return false;
            }
            
            // Rolü kontrol et
            Role role = roleDAO.findById(newRoleId);
            if (role == null) {
                System.out.println("❌ Geçersiz rol!");
                return false;
            }
            
            // Kendini admin'den çıkarmasını engelle
            if (user.getId() == authService.getCurrentUserId() && 
                authService.isCurrentUserAdmin() && !"admin".equals(role.getRoleName())) {
                System.out.println("❌ Kendi admin yetkilerinizi kaldıramazsınız!");
                return false;
            }
            
            // Güncelle
            user.setRoleId(newRoleId);
            boolean success = userDAO.update(user);
            
            if (success) {
                System.out.println("✅ Kullanıcı rolü güncellendi: " + user.getUsername() + " -> " + role.getRoleName());
                return true;
            } else {
                System.out.println("❌ Rol güncellenemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Rol güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kullanıcı bilgilerini günceller (admin için)
     */
    public boolean updateUser(User user) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        try {
            // Mevcut kullanıcıyı kontrol et
            User existingUser = userDAO.findById(user.getId());
            if (existingUser == null) {
                System.out.println("❌ Kullanıcı bulunamadı!");
                return false;
            }
            
            // Email değişmişse kontrol et
            if (!existingUser.getEmail().equals(user.getEmail())) {
                if (userDAO.isEmailExists(user.getEmail(), user.getId())) {
                    System.out.println("❌ Bu email adresi zaten kullanılıyor!");
                    return false;
                }
            }
            
            // Kendini admin'den çıkarmasını engelle
            if (user.getId() == authService.getCurrentUserId() && 
                authService.isCurrentUserAdmin() && user.getRoleId() != 1) {
                System.out.println("❌ Kendi admin yetkilerinizi kaldıramazsınız!");
                return false;
            }
            
            boolean success = userDAO.update(user);
            
            if (success) {
                System.out.println("✅ Kullanıcı güncellendi: " + user.getUsername());
                return true;
            } else {
                System.out.println("❌ Kullanıcı güncellenemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Yeni kullanıcı oluşturur (admin için)
     */
    public boolean createUser(User user) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        try {
            // Email kontrolü
            if (userDAO.isEmailExists(user.getEmail(), 0)) {
                System.out.println("❌ Bu email adresi zaten kullanılıyor!");
                return false;
            }
            
            User savedUser = userDAO.save(user);
            
            if (savedUser != null) {
                System.out.println("✅ Kullanıcı oluşturuldu: " + user.getUsername());
                return true;
            } else {
                System.out.println("❌ Kullanıcı oluşturulamadı!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı oluşturma hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcıyı siler (admin için)
     */
    public boolean deleteUser(int userId) {
        if (!authService.requireAdmin()) {
            return false;
        }
        
        try {
            // Kullanıcıyı bul
            User user = userDAO.findById(userId);
            if (user == null) {
                System.out.println("❌ Kullanıcı bulunamadı!");
                return false;
            }
            
            // Kendini silmesini engelle
            if (user.getId() == authService.getCurrentUserId()) {
                System.out.println("❌ Kendi hesabınızı silemezsiniz!");
                return false;
            }
            
            boolean success = userDAO.delete(userId);
            
            if (success) {
                System.out.println("✅ Kullanıcı silindi: " + user.getUsername());
                return true;
            } else {
                System.out.println("❌ Kullanıcı silinemedi!");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı silme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tüm rolleri getirir
     */
    public List<Role> getAllRoles() {
        try {
            return roleDAO.findAll();
        } catch (SQLException e) {
            System.err.println("❌ Roller yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Kullanıcı yaş hesaplama
     */
    public Integer calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return null;
        }
        
        LocalDate now = LocalDate.now();
        return now.getYear() - birthdate.getYear();
    }
    
    /**
     * Kullanıcı istatistiklerini getirir (admin için)
     */
    public UserStatistics getUserStatistics() {
        if (!authService.requireAdmin()) {
            return null;
        }
        
        try {
            List<User> allUsers = userDAO.findAll();
            
            int totalUsers = allUsers.size();
            int adminCount = 0;
            int sporcuCount = 0;
            int otherCount = 0;
            
            for (User user : allUsers) {
                if (user.isAdmin()) {
                    adminCount++;
                } else if (user.isSporcu()) {
                    sporcuCount++;
                } else {
                    otherCount++;
                }
            }
            
            return new UserStatistics(totalUsers, adminCount, sporcuCount, otherCount);
            
        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı istatistikleri hesaplama hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Kullanıcı istatistikleri için veri sınıfı
     */
    public static class UserStatistics {
        public final int totalUsers;
        public final int adminCount;
        public final int sporcuCount;
        public final int otherCount;
        
        public UserStatistics(int totalUsers, int adminCount, int sporcuCount, int otherCount) {
            this.totalUsers = totalUsers;
            this.adminCount = adminCount;
            this.sporcuCount = sporcuCount;
            this.otherCount = otherCount;
        }
        
        @Override
        public String toString() {
            return String.format(
                "👥 Kullanıcı İstatistikleri:\n" +
                "- Toplam Kullanıcı: %d\n" +
                "- Admin: %d\n" +
                "- Sporcu: %d\n" +
                "- Diğer: %d",
                totalUsers, adminCount, sporcuCount, otherCount
            );
        }
    }
} 