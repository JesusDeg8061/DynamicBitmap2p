package com.dynamicbitmap.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FileChunker {

    public static List<byte[]> splitFile(String path, int chunkSize) throws IOException {

        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);

        List<byte[]> chunks = new ArrayList<>();

        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {

            byte[] chunk;

            if (bytesRead < chunkSize) {
                chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            } else {
                chunk = buffer.clone();
            }

            chunks.add(chunk);
        }

        fis.close();
        return chunks;
    }
 
}