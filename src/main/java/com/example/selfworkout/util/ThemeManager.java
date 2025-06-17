package com.example.selfworkout.util;

import javafx.scene.Scene;
import javafx.scene.Parent;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_PREF = "theme";
    private static ThemeManager instance;
    private final Preferences preferences;

    private ThemeManager() {
        preferences = Preferences.userNodeForPackage(ThemeManager.class);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void applyTheme(Scene scene) {
        String currentTheme = preferences.get(THEME_PREF, "light");
        applyTheme(scene, currentTheme);
    }

    public void applyTheme(Scene scene, String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/com/example/selfworkout/css/" + theme + ".css").toExternalForm());
        preferences.put(THEME_PREF, theme);
    }
}
