package com.dynamicbitmap.core;

import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream; //  IMPORTANTE
import java.util.Map;

public class FileAssembler {

    //  método original (NO se toca)
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

    //  NUEVO MÉTODO (CLAVE PARA CIFRADO)
    public static byte[] assembleToBytes(Map<Integer, byte[]> chunks) {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (int i = 0; i < chunks.size(); i++) {

                byte[] data = chunks.get(i);

                if (data != null) {
                    baos.write(data);
                }
            }

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}