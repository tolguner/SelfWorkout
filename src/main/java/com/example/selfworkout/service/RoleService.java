package com.example.selfworkout.service;

import com.example.selfworkout.dao.RoleDAO;
import com.example.selfworkout.dao.UserDAO;
import com.example.selfworkout.model.Role;
import com.example.selfworkout.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Role için Business Logic sınıfı
 * Kullanıcı rol yönetimi işlemlerini koordine eder
 */
public class RoleService {
    
    private final RoleDAO roleDAO;
    private final UserDAO userDAO;
    
    public RoleService() {
        this.roleDAO = new RoleDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Yeni rol oluşturur
     */
    public Role createRole(Role role) throws SQLException {
        // Validasyon
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rol adı boş olamaz!");
        }
        
        // Aynı isimde rol var mı kontrol et
        Role existingRole = roleDAO.findByName(role.getRoleName());
        if (existingRole != null) {
            throw new IllegalArgumentException("Bu isimde bir rol zaten mevcut!");
        }
        
        Role savedRole = roleDAO.save(role);
        System.out.println("✅ Rol başarıyla oluşturuldu: " + savedRole.getRoleName());
        
        return savedRole;
    }
    
    /**
     * Rolü günceller
     */
    public boolean updateRole(Role role) throws SQLException {
        // Validasyon
        if (role.getId() <= 0) {
            throw new IllegalArgumentException("Geçerli bir rol ID'si gerekli!");
        }
        
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rol adı boş olamaz!");
        }
        
        // Rol var mı kontrol et
        Role existingRole = roleDAO.findById(role.getId());
        if (existingRole == null) {
            throw new IllegalArgumentException("Güncellenecek rol bulunamadı!");
        }
        
        return roleDAO.update(role);
    }
    
    /**
     * Rolü siler
     */
    public boolean deleteRole(int roleId) throws SQLException {
        // Rol var mı kontrol et
        Role role = roleDAO.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Silinecek rol bulunamadı!");
        }
        
        // Bu rolü kullanan kullanıcılar var mı kontrol et (basit implementasyon)
        List<User> allUsers = userDAO.findAll();
        List<User> usersWithRole = allUsers.stream()
            .filter(user -> user.getRoleId() == roleId)
            .collect(Collectors.toList());
        
        if (!usersWithRole.isEmpty()) {
            throw new IllegalArgumentException("Bu rolü kullanan kullanıcılar var! Önce onları güncelleyin.");
        }
        
        boolean deleted = roleDAO.delete(roleId);
        
        if (deleted) {
            System.out.println("✅ Rol başarıyla silindi: " + role.getRoleName());
        }
        
        return deleted;
    }
    
    /**
     * Tüm rolleri getirir
     */
    public List<Role> getAllRoles() throws SQLException {
        return roleDAO.findAll();
    }
    
    /**
     * ID'ye göre rol getirir
     */
    public Role getRoleById(int roleId) throws SQLException {
        if (roleId <= 0) {
            throw new IllegalArgumentException("Geçerli bir rol ID'si gerekli!");
        }
        
        return roleDAO.findById(roleId);
    }
    
    /**
     * İsme göre rol getirir
     */
    public Role getRoleByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rol adı boş olamaz!");
        }
        
        return roleDAO.findByName(name);
    }
    
    /**
     * Rol sayısını getirir
     */
    public int getRoleCount() throws SQLException {
        return roleDAO.findAll().size();
    }
    
    /**
     * Kullanıcıya rol atar
     */
    public boolean assignRoleToUser(int userId, int roleId) throws SQLException {
        // Validasyon
        if (userId <= 0) {
            throw new IllegalArgumentException("Geçerli bir kullanıcı ID'si gerekli!");
        }
        
        if (roleId <= 0) {
            throw new IllegalArgumentException("Geçerli bir rol ID'si gerekli!");
        }
        
        // Kullanıcı var mı kontrol et
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı!");
        }
        
        // Rol var mı kontrol et
        Role role = roleDAO.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Rol bulunamadı!");
        }
        
        // Kullanıcının rolünü güncelle
        user.setRoleId(roleId);
        boolean updated = userDAO.update(user);
        
        if (updated) {
            System.out.println("✅ Kullanıcıya rol atandı: " + user.getUsername() + " -> " + role.getRoleName());
        }
        
        return updated;
    }
    
    /**
     * Kullanıcının rolünü kaldırır (varsayılan role atar)
     */
    public boolean removeRoleFromUser(int userId) throws SQLException {
        // Varsayılan rol ID'si (genellikle "USER" rolü)
        Role defaultRole = getRoleByName("USER");
        if (defaultRole == null) {
            // Varsayılan rol yoksa oluştur
            defaultRole = createDefaultUserRole();
        }
        
        return assignRoleToUser(userId, defaultRole.getId());
    }
    
    /**
     * Belirli role sahip kullanıcıları getirir
     */
    public List<User> getUsersByRole(int roleId) throws SQLException {
        if (roleId <= 0) {
            throw new IllegalArgumentException("Geçerli bir rol ID'si gerekli!");
        }
        
        // Basit implementasyon - tüm kullanıcıları al ve filtrele
        List<User> allUsers = userDAO.findAll();
        return allUsers.stream()
            .filter(user -> user.getRoleId() == roleId)
            .collect(Collectors.toList());
    }
    
    /**
     * Belirli role sahip kullanıcıları getirir (rol adına göre)
     */
    public List<User> getUsersByRoleName(String roleName) throws SQLException {
        Role role = getRoleByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Rol bulunamadı: " + roleName);
        }
        
        return getUsersByRole(role.getId());
    }
    
    /**
     * Admin kullanıcıları getirir
     */
    public List<User> getAdminUsers() throws SQLException {
        return getUsersByRoleName("ADMIN");
    }
    
    /**
     * Normal kullanıcıları getirir
     */
    public List<User> getNormalUsers() throws SQLException {
        return getUsersByRoleName("USER");
    }
    
    /**
     * Kullanıcının admin olup olmadığını kontrol eder
     */
    public boolean isUserAdmin(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        Role role = roleDAO.findById(user.getRoleId());
        return role != null && "ADMIN".equals(role.getRoleName());
    }
    
    /**
     * Kullanıcının belirli role sahip olup olmadığını kontrol eder
     */
    public boolean hasUserRole(int userId, String roleName) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        Role role = roleDAO.findById(user.getRoleId());
        return role != null && roleName.equals(role.getRoleName());
    }
    
    /**
     * Rol istatistiklerini getirir
     */
    public List<RoleStats> getRoleStatistics() throws SQLException {
        List<Role> allRoles = getAllRoles();
        
        return allRoles.stream()
            .map(role -> {
                try {
                    int userCount = getUsersByRole(role.getId()).size();
                    return new RoleStats(role, userCount);
                } catch (SQLException e) {
                    System.err.println("Rol istatistiği alınırken hata: " + e.getMessage());
                    return new RoleStats(role, 0);
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Varsayılan rolleri oluşturur
     */
    public void createDefaultRoles() throws SQLException {
        // ADMIN rolü
        if (getRoleByName("ADMIN") == null) {
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setDescription("Sistem yöneticisi");
            createRole(adminRole);
        }
        
        // USER rolü
        if (getRoleByName("USER") == null) {
            Role userRole = new Role();
            userRole.setRoleName("USER");
            userRole.setDescription("Normal kullanıcı");
            createRole(userRole);
        }
        
        // TRAINER rolü
        if (getRoleByName("TRAINER") == null) {
            Role trainerRole = new Role();
            trainerRole.setRoleName("TRAINER");
            trainerRole.setDescription("Antrenör");
            createRole(trainerRole);
        }
        
        System.out.println("✅ Varsayılan roller oluşturuldu.");
    }
    
    /**
     * Varsayılan USER rolü oluşturur
     */
    private Role createDefaultUserRole() throws SQLException {
        Role userRole = new Role();
        userRole.setRoleName("USER");
        userRole.setDescription("Normal kullanıcı");
        return createRole(userRole);
    }
    
    /**
     * Rol validasyonu
     */
    public boolean isValidRole(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Minimum uzunluk kontrolü
        if (name.trim().length() < 2) {
            return false;
        }
        
        // Sadece harf ve alt çizgi
        return name.matches("^[A-Z_]+$");
    }
    
    /**
     * Rol istatistikleri için yardımcı sınıf
     */
    public static class RoleStats {
        private final Role role;
        private final int userCount;
        
        public RoleStats(Role role, int userCount) {
            this.role = role;
            this.userCount = userCount;
        }
        
        public Role getRole() { return role; }
        public int getUserCount() { return userCount; }
        
        @Override
        public String toString() {
            return String.format("%s (%d kullanıcı)", role.getRoleName(), userCount);
        }
    }
} 