module com.example.selfworkout {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;

    // FXML dosyalarının bu paketlerdeki controller'lara erişimini sağlar
    opens com.example.selfworkout to javafx.fxml;
    opens com.example.selfworkout.controller to javafx.fxml;
    opens com.example.selfworkout.controller.admin to javafx.fxml; // YENİ EKLENDİ
    opens com.example.selfworkout.controller.user to javafx.fxml;  // YENİ EKLENDİ

    // Model sınıflarının JavaFX tabanı tarafından erişimini sağlar (tablolarda vs.)
    opens com.example.selfworkout.model to javafx.base;

    // Diğer modüllerin bu paketlere erişimini sağlar (eğer kullanılıyorsa)
    exports com.example.selfworkout;
    exports com.example.selfworkout.controller;
    exports com.example.selfworkout.controller.admin; // YENİ EKLENDİ
    exports com.example.selfworkout.controller.user;  // YENİ EKLENDİ
    exports com.example.selfworkout.model;
    exports com.example.selfworkout.util;
    exports com.example.selfworkout.service;
}