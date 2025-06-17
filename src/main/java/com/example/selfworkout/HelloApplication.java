package com.example.selfworkout;

import com.example.selfworkout.util.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

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
            if (controller instanceof com.example.selfworkout.controller.BaseController) {
                ((com.example.selfworkout.controller.BaseController) controller).setStage(stage);
            }
            
            // Ekran boyutlarını al (DPI ölçeklendirmesi dahil)
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getBounds().getWidth();
            double screenHeight = screen.getBounds().getHeight();
            
            // Pencere boyutları - Sabit 1536x864 (Sıkı Kontrol)
            stage.setResizable(false);
            stage.setWidth(1536);
            stage.setHeight(864);
            stage.setMinWidth(1536);
            stage.setMinHeight(864);
            stage.setMaxWidth(1536);
            stage.setMaxHeight(864);
            stage.setResizable(true);
            
            // Pencereyi merkeze al
            stage.centerOnScreen();
            
            // Maksimize edilmiş başlat
            stage.setMaximized(true);
            
            // Temayı uygula
            themeManager.setScene(scene);
            themeManager.applyTheme(themeManager.getCurrentTheme());
            
            // Pencereyi göster
            stage.show();
            
            // Uygulama kapanırken temizlik
            stage.setOnCloseRequest(e -> {
                System.out.println("🔄 Uygulama kapatılıyor...");
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