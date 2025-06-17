package com.example.selfworkout;

import com.example.selfworkout.util.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination; // Bu import kullanılmıyor, kaldırılabilir
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane; // Bu import kullanılmıyor, kaldırılabilir
import javafx.scene.Node; // Bu import kullanılmıyor, kaldırılabilir
import javafx.beans.value.ChangeListener; // Bu import kullanılmıyor, kaldırılabilir
import javafx.beans.value.ObservableValue; // Bu import kullanılmıyor, kaldırılabilir
import javafx.scene.control.Tooltip; // Bu import kullanılmıyor, kaldırılabilir
import javafx.util.Duration; // Bu import kullanılmıyor, kaldırılabilir

import java.io.IOException;

/**
 * SelfWorkout JavaFX Uygulaması
 * Fitness takip ve antrenman yönetimi
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // UTF-8 encoding ayarları - Türkçe karakter desteği için
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");

            // ThemeManager'ı başlat
            ThemeManager themeManager = ThemeManager.getInstance();

            // Login sahnesi oluştur - Sabit boyutlu
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1536, 864);

            // Tam ekran ayarları
            stage.setTitle("SelfWorkout - Fitness Takip Uygulaması");
            stage.setScene(scene);

            // Controller'a stage referansını ver
            Object controller = fxmlLoader.getController();
            // Bu kısımda BaseController'a cast etmeden önce null kontrolü yapmak daha güvenli
            // Ayrıca, LoginController BaseController'dan miras almadığı için farklı ele alınmalı.
            // Bu logic'in SceneManager içinde olması daha uygundur.
            // Ancak, uygulamanın başladığı nokta olduğu için burayı da güncelleyelim:
            if (controller instanceof com.example.selfworkout.controller.LoginController) {
                ((com.example.selfworkout.controller.LoginController) controller).setStage(stage);
            }
            // HelloApplication, LoginController'ı yüklediği için, diğer controller türlerine cast etmeye
            // burada gerek yoktur. Bu SceneManager'ın görevidir.

            // Ekran boyutlarını al (DPI ölçeklendirmesi dahil)
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getBounds().getWidth();
            double screenHeight = screen.getBounds().getHeight();

            // Pencere boyutları - Sabit 1536x864 (Sıkı Kontrol)
            // Eğer stage.setMaximized(true) kullanıyorsanız, bu sabit boyutlar çelişkilidir.
            // Ya sabit boyutlandırma yapın VE setResizable(false) yapın,
            // ya da tamamen esnek olun ve maximize edin.
            // Genellikle sabit boyut + maximize istenmez. Maximize modu esneklik demektir.
            // Şimdilik çelişen kodları kaldırdım ve sadece maksimum boyutu zorlayıp sonra maximize ediyorum.
            stage.setResizable(true); // Kullanıcının boyutlandırmasına izin ver
            stage.setMinWidth(1536);
            stage.setMinHeight(864);
            // stage.setMaxWidth(1536); // Eğer maksimize ediyorsanız bu satır sorun yaratabilir
            // stage.setMaxHeight(864); // Eğer maksimize ediyorsanız bu satır sorun yaratabilir

            // Pencereyi merkeze al (maksimize edilmeden önce)
            stage.centerOnScreen();

            // Maksimize edilmiş başlat
            stage.setMaximized(true);

            // Temayı uygula
            // DÜZELTİLDİ: themeManager.setScene(scene) yerine doğrudan applyTheme metodu çağrıldı
            themeManager.applyTheme(scene);

            // Pencereyi göster
            stage.show();

            // Uygulama kapanırken temizlik
            stage.setOnCloseRequest(e -> {
                System.out.println("🔄 Uygulama kapatılıyor...");
                // DatabaseConnection'ı kapatmak için ekledim.
                com.example.selfworkout.util.DatabaseConnection.closeConnection();
                Platform.exit();
            });

            System.out.println("✅ SelfWorkout uygulaması başlatıldı - Maksimize Mod");
            System.out.println("📐 Ekran boyutu: " + screenWidth + "x" + screenHeight);

        } catch (Exception e) {
            System.err.println("❌ Uygulama başlatma hatası: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}