package com.dynamicbitmap.core;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.scene.Scene;

public class ThemeManager {

    private static final Preferences prefs =
            Preferences.userRoot()
                    .node("DynamicBitmap");

    private static final List<Scene> scenes =
            new ArrayList<>();

    public static boolean isDarkMode() {

        return prefs.getBoolean(
                "darkMode",
                false
        );
    }

    public static void setDarkMode(
            boolean dark
    ) {

        prefs.putBoolean(
                "darkMode",
                dark
        );
    }

    public static void registerScene(
            Scene scene
    ) {

        if (!scenes.contains(scene)) {

            scenes.add(scene);
        }
    }

    public static void applyThemeToAllScenes() {

        for (Scene scene : scenes) {

            scene.getStylesheets().clear();

            if (isDarkMode()) {

                scene.getStylesheets().add(
                        ThemeManager.class
                                .getResource(
                                        "/com/dynamicbitmap/ui/fx/matrix.css"
                                )
                                .toExternalForm()
                );

            } else {

                scene.getStylesheets().add(
                        ThemeManager.class
                                .getResource(
                                        "/com/dynamicbitmap/ui/fx/app.css"
                                )
                                .toExternalForm()
                );
            }
        }
    }
}