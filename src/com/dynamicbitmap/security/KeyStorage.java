package com.dynamicbitmap.security;

import java.io.File;
import java.nio.file.Files;

public class KeyStorage {

    private static final String BASE_DIR = "keys";

    public static void saveKey(
            String nodeId,
            String fileId,
            byte[] keyBytes
    ) {

        try {

            if (nodeId == null || fileId == null || keyBytes == null) {
                return;
            }

            File dir =
                    new File(
                            BASE_DIR + "/" + nodeId
                    );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file =
                    new File(
                            dir,
                            safeFileName(fileId) + ".key"
                    );

            Files.write(
                    file.toPath(),
                    keyBytes
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static byte[] loadKey(
            String nodeId,
            String fileId
    ) {

        try {

            if (nodeId == null || fileId == null) {
                return null;
            }

            File file =
                    new File(
                            BASE_DIR
                                    + "/"
                                    + nodeId
                                    + "/"
                                    + safeFileName(fileId)
                                    + ".key"
                    );

            if (!file.exists()) {
                return null;
            }

            return Files.readAllBytes(
                    file.toPath()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static boolean exists(
            String nodeId,
            String fileId
    ) {

        if (nodeId == null || fileId == null) {
            return false;
        }

        File file =
                new File(
                        BASE_DIR
                                + "/"
                                + nodeId
                                + "/"
                                + safeFileName(fileId)
                                + ".key"
                );

        return file.exists();
    }

    private static String safeFileName(String value) {

        return value
                .replace("/", "_")
                .replace("\\", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_");
    }
}