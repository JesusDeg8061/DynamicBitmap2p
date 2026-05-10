package com.dynamicbitmap.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileInputStream;

import java.security.MessageDigest;

public class CryptoUtils {

    //  generar clave AES
    public static SecretKey generateKey() throws Exception {

        KeyGenerator keyGen =
                KeyGenerator.getInstance("AES");

        keyGen.init(128);

        return keyGen.generateKey();
    }

    //  cifrar
    public static byte[] encrypt(
            byte[] data,
            SecretKey key
    ) throws Exception {

        Cipher cipher =
                Cipher.getInstance("AES");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key
        );

        return cipher.doFinal(data);
    }

    //  descifrar
    public static byte[] decrypt(
            byte[] data,
            SecretKey key
    ) throws Exception {

        Cipher cipher =
                Cipher.getInstance("AES");

        cipher.init(
                Cipher.DECRYPT_MODE,
                key
        );

        return cipher.doFinal(data);
    }

    //  SHA-256 desde bytes
    public static String sha256(
            byte[] data
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(data);

        StringBuilder sb =
                new StringBuilder();

        for (byte b : hash) {

            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }

    //  SHA-256 desde archivo
    public static String sha256(
            File file
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        FileInputStream fis =
                new FileInputStream(file);

        byte[] buffer =
                new byte[8192];

        int read;

        while ((read = fis.read(buffer)) != -1) {

            digest.update(buffer, 0, read);
        }

        fis.close();

        byte[] hash =
                digest.digest();

        StringBuilder sb =
                new StringBuilder();

        for (byte b : hash) {

            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }

    //  reconstruir clave desde bytes
    public static SecretKey fromBytes(
            byte[] keyBytes
    ) {

        return new SecretKeySpec(
                keyBytes,
                "AES"
        );
    }
}