package com.example.selfworkout.service;

import com.example.selfworkout.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation için Business Logic sınıfı
 * Veri doğrulama ve iş kuralları kontrolü yapar
 */
public class ValidationService {

    // Regex patterns - DÜZELTİLDİ: 'quot;' yerine '"' ve sonuna '$' eklendi
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+90|0)?[5][0-9]{9}$"
    );

    /**
     * Kullanıcı validasyonu
     */
    public ValidationResult validateUser(User user) {
        ValidationResult result = new ValidationResult();

        if (user == null) {
            result.addError("Kullanıcı bilgisi boş olamaz!");
            return result;
        }

        // Username kontrolü
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.addError("Kullanıcı adı boş olamaz!");
        } else if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            result.addError("Kullanıcı adı 3-20 karakter arası olmalı ve sadece harf, rakam, alt çizgi içermelidir!");
        }

        // Email kontrolü
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            result.addError("E-posta adresi boş olamaz!");
        } else if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            result.addError("Geçerli bir e-posta adresi giriniz!");
        }

        // Password kontrolü
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.addError("Şifre boş olamaz!");
        } else if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            result.addError("Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam içermelidir!");
        }

        // Doğum tarihi kontrolü (yaş yerine)
        if (user.getBirthdate() != null) {
            LocalDate now = LocalDate.now();
            int age = now.getYear() - user.getBirthdate().getYear();
            if (user.getBirthdate().plusYears(age).isAfter(now)) {
                age--;
            }
            if (age < 13 || age > 120) {
                result.addError("Yaş 13-120 arasında olmalıdır!");
            }
        }

        return result;
    }

    /**
     * Egzersiz validasyonu
     */
    public ValidationResult validateExercise(Exercise exercise) {
        ValidationResult result = new ValidationResult();

        if (exercise == null) {
            result.addError("Egzersiz bilgisi boş olamaz!");
            return result;
        }

        // İsim kontrolü
        if (exercise.getName() == null || exercise.getName().trim().isEmpty()) {
            result.addError("Egzersiz adı boş olamaz!");
        } else if (exercise.getName().length() < 2 || exercise.getName().length() > 100) {
            result.addError("Egzersiz adı 2-100 karakter arasında olmalıdır!");
        }

        // Açıklama kontrolü
        if (exercise.getDescription() != null && exercise.getDescription().length() > 1000) {
            result.addError("Egzersiz açıklaması 1000 karakterden uzun olamaz!");
        }

        // Zorluk seviyesi kontrolü
        if (exercise.getDifficultyLevel() != null) {
            String difficulty = exercise.getDifficultyLevel().toLowerCase();
            if (!difficulty.equals("beginner") && !difficulty.equals("intermediate") &&
                    !difficulty.equals("advanced") && !difficulty.equals("başlangıç") &&
                    !difficulty.equals("orta") && !difficulty.equals("ileri")) {
                result.addError("Geçerli zorluk seviyeleri: Başlangıç, Orta, İleri");
            }
        }

        return result;
    }

    /**
     * Vücut istatistikleri validasyonu
     */
    public ValidationResult validateBodyStats(BodyStats bodyStats) {
        ValidationResult result = new ValidationResult();

        if (bodyStats == null) {
            result.addError("Vücut istatistikleri boş olamaz!");
            return result;
        }

        // Kullanıcı ID kontrolü
        if (bodyStats.getUserId() <= 0) {
            result.addError("Geçerli bir kullanıcı ID'si gerekli!");
        }

        // Kilo kontrolü
        if (bodyStats.getWeight() <= 0 || bodyStats.getWeight() > 500) {
            result.addError("Kilo 0-500 kg arasında olmalıdır!");
        }

        // Boy kontrolü
        if (bodyStats.getHeight() <= 0 || bodyStats.getHeight() > 300) {
            result.addError("Boy 0-300 cm arasında olmalıdır!");
        }

        // Vücut yağ oranı kontrolü
        if (bodyStats.getBodyFat() < 0 || bodyStats.getBodyFat() > 100) {
            result.addError("Vücut yağ oranı 0-100% arasında olmalıdır!");
        }

        // Kas kütlesi kontrolü
        if (bodyStats.getMuscleMass() < 0 || bodyStats.getMuscleMass() > bodyStats.getWeight()) {
            result.addError("Kas kütlesi 0 ile toplam kilo arasında olmalıdır!");
        }

        // Tarih kontrolü
        if (bodyStats.getRecordDate() == null) {
            result.addError("Kayıt tarihi gerekli!");
        } else if (bodyStats.getRecordDate().isAfter(LocalDate.now())) {
            result.addError("Kayıt tarihi gelecekte olamaz!");
        }

        return result;
    }

    /**
     * Günlük antrenman validasyonu
     */
    public ValidationResult validateDailyWorkout(DailyWorkout workout) {
        ValidationResult result = new ValidationResult();

        if (workout == null) {
            result.addError("Antrenman bilgisi boş olamaz!");
            return result;
        }

        // Kullanıcı ID kontrolü
        if (workout.getUserId() <= 0) {
            result.addError("Geçerli bir kullanıcı ID'si gerekli!");
        }

        // Tarih kontrolü
        if (workout.getWorkoutDate() == null) {
            result.addError("Antrenman tarihi gerekli!");
        } else if (workout.getWorkoutDate().isAfter(LocalDate.now())) {
            result.addError("Antrenman tarihi gelecekte olamaz!");
        }

        // Süre kontrolü
        if (workout.getTotalDuration() < 0 || workout.getTotalDuration() > 600) {
            result.addError("Antrenman süresi 0-600 dakika arasında olmalıdır!");
        }

        return result;
    }

    /**
     * Rutin validasyonu
     */
    public ValidationResult validateExerciseRoutine(ExerciseRoutine routine) {
        ValidationResult result = new ValidationResult();

        if (routine == null) {
            result.addError("Rutin bilgisi boş olamaz!");
            return result;
        }

        // Kullanıcı ID kontrolü
        if (routine.getUserId() <= 0) {
            result.addError("Geçerli bir kullanıcı ID'si gerekli!");
        }

        // İsim kontrolü
        if (routine.getName() == null || routine.getName().trim().isEmpty()) {
            result.addError("Rutin adı boş olamaz!");
        } else if (routine.getName().length() < 2 || routine.getName().length() > 100) {
            result.addError("Rutin adı 2-100 karakter arasında olmalıdır!");
        }

        // Açıklama kontrolü
        if (routine.getDescription() != null && routine.getDescription().length() > 500) {
            result.addError("Rutin açıklaması 500 karakterden uzun olamaz!");
        }

        return result;
    }

    /**
     * Tarih aralığı validasyonu
     */
    public ValidationResult validateDateRange(LocalDate startDate, LocalDate endDate) {
        ValidationResult result = new ValidationResult();

        if (startDate == null) {
            result.addError("Başlangıç tarihi gerekli!");
        }

        if (endDate == null) {
            result.addError("Bitiş tarihi gerekli!");
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                result.addError("Başlangıç tarihi bitiş tarihinden sonra olamaz!");
            }

            if (startDate.isAfter(LocalDate.now())) {
                result.addError("Başlangıç tarihi gelecekte olamaz!");
            }

            if (endDate.isAfter(LocalDate.now())) {
                result.addError("Bitiş tarihi gelecekte olamaz!");
            }
        }

        return result;
    }

    /**
     * E-posta validasyonu
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kullanıcı adı validasyonu
     */
    public boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Şifre validasyonu
     */
    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Telefon numarası validasyonu
     */
    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Pozitif sayı validasyonu
     */
    public boolean isPositiveNumber(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    /**
     * Aralık validasyonu
     */
    public boolean isInRange(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) {
            return false;
        }

        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();

        return val >= minVal && val <= maxVal;
    }

    /**
     * Validasyon sonucu için yardımcı sınıf
     */
    public static class ValidationResult {
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult() {
            this.errors = new java.util.ArrayList<>();
            this.warnings = new java.util.ArrayList<>();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }

        public String getWarningMessage() {
            return String.join(", ", warnings);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (!errors.isEmpty()) {
                sb.append("Hatalar: ").append(String.join(", ", errors));
            }

            if (!warnings.isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Uyarılar: ").append(String.join(", ", warnings));
            }

            if (sb.length() == 0) {
                sb.append("Validasyon başarılı");
            }

            return sb.toString();
        }
    }
}