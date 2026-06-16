package com.dynamicbitmap.ui.fx;

import com.dynamicbitmap.core.IdentityManager;
import com.dynamicbitmap.ui.MainUI;
import com.dynamicbitmap.core.ThemeManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Thread backendThread =
                new Thread(() -> {

                    try {

                        String id =
                                IdentityManager.getNodeId();

                        MainUI backend =
                                new MainUI(id);

                        backend.setVisible(false);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                });

        backendThread.setDaemon(true);

        backendThread.start();

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/com/dynamicbitmap/ui/fx/Dashboard.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );
        ThemeManager.registerScene(
        scene
);

ThemeManager.applyThemeToAllScenes();


        stage.setTitle(
                "DynamicBitmap"
        );

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.setOnCloseRequest(event -> {

            System.out.println("CERRANDO APP");

            System.exit(0);
        });

        stage.show();
    }

    @Override
    public void stop() {

        System.out.println("FINALIZANDO DYNAMICBITMAP");

        System.exit(0);
    }

    public static void main(String[] args) {

        launch(args);
    }
}