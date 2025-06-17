package com.example.selfworkout.util;

import javafx.scene.Scene;
import javafx.scene.Parent; // Kullanılmıyorsa kaldırılabilir
import java.net.URL; // URL import'u eklendi
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_PREF = "theme";
    private static ThemeManager instance;
    private final Preferences preferences;
    private Scene currentScene; // Temanın uygulanacağı scene objesi

    private ThemeManager() {
        preferences = Preferences.userNodeForPackage(ThemeManager.class);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // Temanın uygulanacağı Scene'i kaydetmek için (opsiyonel, HelloApplication'da set ediliyor)
    public void setScene(Scene scene) {
        this.currentScene = scene;
    }

    // Scene nesnesini doğrudan alan ve varsayılan temayı uygulayan metot
    public void applyTheme(Scene scene) {
        // Varsayılan temayı "main-theme" olarak değiştiriyoruz, çünkü light.css diye bir dosya yok.
        String currentThemeName = preferences.get(THEME_PREF, "main-theme"); // DÜZELTİLDİ: "light" yerine "main-theme"
        applyTheme(scene, currentThemeName);
    }

    // Belirli bir temayı Scene nesnesine uygulayan metot
    public void applyTheme(Scene scene, String themeName) {
        if (scene == null) {
            System.err.println("❌ Tema uygulanamadı: Scene objesi null.");
            return;
        }

        URL cssResource = getClass().getResource("/com/example/selfworkout/css/" + themeName + ".css");

        if (cssResource == null) {
            System.err.println("❌ Tema dosyası bulunamadı: " + "/com/example/selfworkout/css/" + themeName + ".css");
            // Eğer tema dosyası bulunamazsa, varsayılan bir tema yüklemeye çalışın
            if (!themeName.equals("main-theme")) { // Sonsuz döngüyü önlemek için kontrol
                System.out.println("🔄 Varsayılan 'main-theme' teması yükleniyor...");
                cssResource = getClass().getResource("/com/example/selfworkout/css/main-theme.css");
                if (cssResource == null) {
                    System.err.println("❌ Varsayılan tema 'main-theme.css' bile bulunamadı! Temalar çalışmayabilir.");
                    return; // Hiçbir tema yüklenemedi
                }
            } else {
                return; // "main-theme" zaten denendi ve bulunamadı
            }
        }

        scene.getStylesheets().clear();
        scene.getStylesheets().add(cssResource.toExternalForm());
        preferences.put(THEME_PREF, themeName);
        System.out.println("✅ Tema başarıyla uygulandı: " + themeName);
    }

    // Kaydedilen mevcut tema adını döndürür
    public String getCurrentTheme() {
        return preferences.get(THEME_PREF, "main-theme"); // DÜZELTİLDİ: "light" yerine "main-theme"
    }

    // Tema değiştirme metodu (eğer ThemeManager'da bir Scene tutuluyorsa kullanılabilir)
    public void changeTheme(String newThemeName) {
        if (this.currentScene != null) {
            applyTheme(this.currentScene, newThemeName);
        } else {
            System.err.println("Tema değiştirilemedi: Scene objesi ThemeManager'a atanmamış.");
        }
    }
}