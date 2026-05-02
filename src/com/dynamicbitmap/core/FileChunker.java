package com.dynamicbitmap.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; 

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

    //  NUEVO MÉTODO (CLAVE PARA CIFRADO)
    public static List<byte[]> splitBytes(byte[] data, int chunkSize) {

        List<byte[]> chunks = new ArrayList<>();

        int index = 0;

        while (index < data.length) {

            int end = Math.min(data.length, index + chunkSize);

            byte[] chunk = Arrays.copyOfRange(data, index, end);

            chunks.add(chunk);

            index += chunkSize;
        }

        return chunks;
    }
}