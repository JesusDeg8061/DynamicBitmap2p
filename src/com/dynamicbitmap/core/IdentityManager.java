package com.dynamicbitmap.core;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class IdentityManager {

    private static final String DIR = "identity";
    private static final String PUB_FILE = DIR + "/public.key";
    private static final String PRIV_FILE = DIR + "/private.key";

    private static KeyPair keyPair;

    //  Obtener o crear identidad
    public static KeyPair getOrCreateIdentity() {
        try {
            if (Files.exists(Paths.get(PUB_FILE)) && Files.exists(Paths.get(PRIV_FILE))) {
                return loadKeys();
            } else {
                return generateAndSave();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error identidad", e);
        }
    }

    // 🔹 Generar claves
    private static KeyPair generateAndSave() throws Exception {

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        Files.createDirectories(Paths.get(DIR));

        // guardar claves
        writeFile(PUB_FILE, keyPair.getPublic().getEncoded());
        writeFile(PRIV_FILE, keyPair.getPrivate().getEncoded());

        return keyPair;
    }

    // 🔹 Cargar claves
    private static KeyPair loadKeys() throws Exception {

        byte[] pubBytes = Files.readAllBytes(Paths.get(PUB_FILE));
        byte[] privBytes = Files.readAllBytes(Paths.get(PRIV_FILE));

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
        PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

        keyPair = new KeyPair(pub, priv);
        return keyPair;
    }

    private static void writeFile(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }

    //  ID del nodo (hash de clave pública)
    public static String getNodeId() {
        try {
            KeyPair kp = getOrCreateIdentity();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(kp.getPublic().getEncoded());

            return Base64.getEncoder().encodeToString(hash).substring(0, 16);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}