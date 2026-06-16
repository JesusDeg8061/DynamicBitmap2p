package com.dynamicbitmap.ui.fx;

import com.dynamicbitmap.core.AppContext;
import com.dynamicbitmap.metadata.FileMetadata;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

public class ArchivosController {
    
    

    @FXML
    private ListView<String> filesList;

    private int lastFileCount = -1;

    @FXML
    public void initialize() {

        refreshFiles();

        Thread updater =
                new Thread(() -> {

                    while (true) {

                        try {

                            Thread.sleep(2000);

                            Platform.runLater(
                                    this::refreshFiles
                            );

                        } catch (Exception e) {

                            break;
                        }
                    }
                });

        updater.setDaemon(true);

        updater.start();
    }

    @FXML
    private void refreshFiles() {

        if (AppContext.node == null) {

            if (filesList.getItems().isEmpty()) {

                filesList.getItems().add(
                        "No conectado a la red"
                );
            }

            return;
        }

        int currentCount =
                AppContext.node
                        .getFiles()
                        .size();

        if (currentCount == lastFileCount) {
            return;
        }

        lastFileCount = currentCount;

        filesList.getItems().clear();

        for (
                FileMetadata metadata :
                AppContext.node.getFiles().values()
        ) {

            String item =
                    "📄 "
                            + metadata.getFileName()
                            + "\n"
                            + formatSize(
                                    metadata.getOriginalSize()
                            );

            filesList.getItems().add(item);
        }

        if (filesList.getItems().isEmpty()) {

            filesList.getItems().add(
                    "No hay archivos disponibles"
            );
        }
    }

    @FXML
    private void downloadSelected() {

        String selected =
                filesList.getSelectionModel()
                        .getSelectedItem();

        if (selected == null
                || selected.startsWith("No ")) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setHeaderText(null);

            alert.setContentText(
                    "Selecciona un archivo."
            );

            alert.showAndWait();

            return;
        }

        Alert alert =
                new Alert(
                            Alert.AlertType.INFORMATION
                    );

        alert.setTitle(
                "DynamicBitmap"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "La descarga real se conectará en el siguiente paso."
        );

        alert.showAndWait();
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
}