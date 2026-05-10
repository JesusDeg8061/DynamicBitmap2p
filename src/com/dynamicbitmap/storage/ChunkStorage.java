package com.dynamicbitmap.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChunkStorage {

    private static final String BASE_DIR = "chunks";

    //  guardar chunk
    public static void saveChunk(
            String nodeId,
            String fileId,
            int chunkId,
            byte[] data
    ) {

        try {

            File dir = new File(
                    BASE_DIR
                            + "/"
                            + nodeId
                            + "/"
                            + fileId
            );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file =
                    new File(
                            dir,
                            chunkId + ".chunk"
                    );

            Files.write(
                    file.toPath(),
                    data
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    //  cargar chunk
    public static byte[] loadChunk(
            String nodeId,
            String fileId,
            int chunkId
    ) {

        try {

            File file =
                    new File(
                            BASE_DIR
                                    + "/"
                                    + nodeId
                                    + "/"
                                    + fileId
                                    + "/"
                                    + chunkId
                                    + ".chunk"
                    );

            if (!file.exists()) {
                return null;
            }

            return Files.readAllBytes(
                    file.toPath()
            );

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    //  obtener carpetas de archivos
    public static File[] loadNodeFiles(
            String nodeId
    ) {

        File dir =
                new File(
                        BASE_DIR + "/" + nodeId
                );

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.listFiles(File::isDirectory);
    }

    //  cargar chunks de archivo específico
    public static File[] loadFileChunks(
            String nodeId,
            String fileId
    ) {

        File dir =
                new File(
                        BASE_DIR
                                + "/"
                                + nodeId
                                + "/"
                                + fileId
                );

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.listFiles(
                (d, name) ->
                        name.endsWith(".chunk")
        );
    }
}