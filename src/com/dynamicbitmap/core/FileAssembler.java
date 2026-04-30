package com.dynamicbitmap.core;

import java.io.FileOutputStream;
import java.util.Map;

public class FileAssembler {

    public static void assemble(Map<Integer, byte[]> chunks, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {

            for (int i = 0; i < chunks.size(); i++) {
                byte[] data = chunks.get(i);
                if (data != null) {
                    fos.write(data);
                }
            }

            System.out.println("Archivo reconstruido en: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}