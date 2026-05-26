package com.dynamicbitmap.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.List;

public class MetadataStorage {

    private static final String BASE_DIR = "metadata";

    public static void saveMetadata(
            String nodeId,
            FileMetadata metadata
    ) {

        try {

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
                            metadata.getFileId() + ".meta"
                    );

            ObjectOutputStream out =
                    new ObjectOutputStream(
                            new FileOutputStream(file)
                    );

            out.writeObject(metadata);

            out.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static FileMetadata loadMetadata(
            String nodeId,
            String fileId
    ) {

        try {

            File file =
                    new File(
                            BASE_DIR
                                    + "/"
                                    + nodeId
                                    + "/"
                                    + fileId
                                    + ".meta"
                    );

            if (!file.exists()) {
                return null;
            }

            ObjectInputStream in =
                    new ObjectInputStream(
                            new FileInputStream(file)
                    );

            FileMetadata metadata =
                    (FileMetadata) in.readObject();

            in.close();

            return metadata;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static List<FileMetadata> loadAllMetadata(
            String nodeId
    ) {

        List<FileMetadata> result =
                new ArrayList<>();

        File dir =
                new File(
                        BASE_DIR + "/" + nodeId
                );

        if (!dir.exists()) {
            dir.mkdirs();
            return result;
        }

        File[] files =
                dir.listFiles(
                        (d, name) ->
                                name.endsWith(".meta")
                );

        if (files == null) {
            return result;
        }

        for (File file : files) {

            try {

                ObjectInputStream in =
                        new ObjectInputStream(
                                new FileInputStream(file)
                        );

                FileMetadata metadata =
                        (FileMetadata) in.readObject();

                in.close();

                result.add(metadata);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return result;
    }
}