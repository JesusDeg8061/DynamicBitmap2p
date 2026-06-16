package com.dynamicbitmap.ui.fx;

import java.io.File;

import com.dynamicbitmap.core.AppContext;
import com.dynamicbitmap.metadata.FileMetadata;
import com.dynamicbitmap.core.ThemeManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class DashboardController {

    private static Stage archivosStage;
    private static Stage redStage;

    private static Stage configuracionStage;

    @FXML
    private Label rebalanceLabel;
    
    @FXML
    private Label filesCountLabel;

    @FXML
    private Label storageLabel;

    @FXML
    private Label sharedSpaceLabel;

    @FXML
    private Label nodesLabel;

    @FXML
    public void initialize() {

        startDashboardUpdater();
    }

    @FXML
    private void uploadFile(
            ActionEvent event
    ) {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Seleccionar archivo"
        );

        File file =
                chooser.showOpenDialog(
                        null
                );

        if (
                file != null
                &&
                AppContext.backend != null
        ) {

            AppContext.backend.uploadFile(
                    file
            );

            Alert alert =
                    new Alert(
                            AlertType.INFORMATION
                    );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "Archivo compartido correctamente"
            );

            alert.showAndWait();
        }
    }

    @FXML
    private void openFiles(
            ActionEvent event
    ) {

        try {

            if (archivosStage != null
                    && archivosStage.isShowing()) {

                archivosStage.toFront();
                archivosStage.requestFocus();

                return;
            }

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/dynamicbitmap/ui/fx/Archivos.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            archivosStage =
                    new Stage();

            Scene scene =
                    new Scene(root);

ThemeManager.registerScene(
        scene
);

ThemeManager.applyThemeToAllScenes();

            archivosStage.setTitle(
                    "Mis Archivos"
            );

            archivosStage.setScene(
                    scene
            );

            archivosStage.setOnHidden(e ->
                    archivosStage = null
            );

            archivosStage.show();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert =
                    new Alert(
                            AlertType.ERROR
                    );

            alert.setTitle(
                    "Error"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "No se pudo abrir la ventana de archivos."
            );

            alert.showAndWait();
        }
    }

    private void startDashboardUpdater() {

        Thread updater =
                new Thread(() -> {

                    while (true) {

                        try {

                            Thread.sleep(2000);

                            Platform.runLater(
                                    this::updateStats
                            );

                        } catch (Exception e) {

                            break;
                        }
                    }
                });

        updater.setDaemon(true);

        updater.start();
    }

    private void updateStats() {

        if (AppContext.node == null) {
            return;
            
        }
int totalFiles =
        AppContext.node
                .getFiles()
                .size();

long totalBytes =
        AppContext.node
                .getCurrentSize();

int onlineNodes =
        AppContext.node
                .getOnlineNodes();

        filesCountLabel.setText(
                String.valueOf(totalFiles)
        );
        
        nodesLabel.setText(
        String.valueOf(
                onlineNodes
        )
);

        String sizeText =
                formatSize(totalBytes);

       storageLabel.setText(
        sizeText
);

sharedSpaceLabel.setText(
        formatSize(
                totalBytes
        )
);

rebalanceLabel.setText(
        String.valueOf(
                AppContext.node
                        .getChunksToMoveCount()
        )
);

nodesLabel.setText(
        String.valueOf(
                onlineNodes
        )
);

    }

    private String formatSize(long size) {

        if (size < 1024)
            return size + " B";

        if (size < 1024 * 1024)
            return (size / 1024) + " KB";

        if (size < 1024 * 1024 * 1024)
            return (size / (1024 * 1024)) + " MB";

        return (size / (1024 * 1024 * 1024)) + " GB";
    }
    
    
    @FXML
private void openRed(
        ActionEvent event
) {

    try {

        if (redStage != null
                && redStage.isShowing()) {

            redStage.toFront();
            redStage.requestFocus();

            return;
        }

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/com/dynamicbitmap/ui/fx/Red.fxml"
                        )
                );

        Parent root =
                loader.load();

        redStage =
                new Stage();

        Scene scene =
                new Scene(root);

ThemeManager.registerScene(
        scene
);

ThemeManager.applyThemeToAllScenes();

        redStage.setTitle(
                "Red"
        );

        redStage.setScene(
                scene
        );

        redStage.setOnHidden(e ->
                redStage = null
        );

        redStage.show();

    } catch (Exception e) {

        e.printStackTrace();
    }
}

@FXML
private void openConfiguracion(
        ActionEvent event
) {

    try {

        if (configuracionStage != null
                && configuracionStage.isShowing()) {

            configuracionStage.toFront();
            configuracionStage.requestFocus();

            return;
        }

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/com/dynamicbitmap/ui/fx/Configuracion.fxml"
                        )
                );

        Parent root =
                loader.load();

        configuracionStage =
                new Stage();

        Scene scene =
                new Scene(root);

       ThemeManager.registerScene(
        scene
);

ThemeManager.applyThemeToAllScenes();

        configuracionStage.setTitle(
                "Configuración"
        );

        configuracionStage.setScene(
                scene
        );

        configuracionStage.setOnHidden(
                e -> configuracionStage = null
        );

        configuracionStage.show();

    } catch (Exception e) {

        e.printStackTrace();
    }
}

}