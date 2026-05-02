package com.dynamicbitmap.ui;

import com.dynamicbitmap.network.PeerDiscovery;
import com.dynamicbitmap.node.Node;
import com.dynamicbitmap.network.NodeServer;
import com.dynamicbitmap.network.NodeInfo;
import com.dynamicbitmap.core.FileChunker;
import com.dynamicbitmap.core.FileAssembler;
import com.dynamicbitmap.security.CryptoUtils;

import javax.swing.*;
import javax.crypto.SecretKey;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class MainUI extends JFrame {

    private int myPort;
    private JTextArea log;
    private Node node;
    //  SOLO UNA VEZ EN TODA LA APP
    //private static boolean monitorStarted = false;

    private Set<String> shownPeers = new HashSet<>();
    private Map<String, Integer> myFiles = new HashMap<>();
    private Map<String, byte[]> fileKeys = new HashMap<>();

    //  colores globales
    private Color bg = new Color(30,30,30);
    private Color logBg = new Color(20,20,20);
    private Color green = new Color(0,255,150);

    public MainUI(String id) {

        setTitle("Nodo - " + id);
        setSize(520, 420);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bg);

        myPort = 5000 + (int)(Math.random() * 1000);

        node = new Node(id, 100);
        new Thread(new NodeServer(myPort, node)).start();
        //  REGISTRAR NODO EN EL MONITOR
        com.dynamicbitmap.network.NetworkEventSender.send("NODE:" + id);

        JLabel label = new JLabel("ID: " + id + " | Puerto: " + myPort);
        label.setForeground(green);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        log = new JTextArea();
        log.setBackground(logBg);
        log.setForeground(green);
        log.setEditable(false);

        add(new JScrollPane(log), BorderLayout.CENTER);

        JButton upload = new JButton("Subir archivo");
        JButton filesBtn = new JButton("Archivos");

        //  estilo botones
        styleButton(upload);
        styleButton(filesBtn);

        //  SUBIR (CON CIFRADO)
        upload.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();
            chooser.showOpenDialog(this);

            try {
                File file = chooser.getSelectedFile();

                if (file != null) {

                    log("📂 Archivo: " + file.getName());

                    byte[] fileData = Files.readAllBytes(file.toPath());

                    //  generar clave
                    SecretKey key = CryptoUtils.generateKey();

                    //  cifrar
                    byte[] encrypted = CryptoUtils.encrypt(fileData, key);

                    //  hash
                    String hash = CryptoUtils.sha256(fileData);

                    log("🔐 ID: " + hash);

                    List<byte[]> chunks =
                            FileChunker.splitBytes(encrypted, 1024);

                    for (int i = 0; i < chunks.size(); i++) {
                        node.storeChunk(i, chunks.get(i));
                    }

                    myFiles.put(file.getName(), chunks.size());
                    fileKeys.put(file.getName(), key.getEncoded());

                    log("📦 " + chunks.size() + " chunks");
                    node.smartReplicate();
                    log("🚀 Distribuyendo...");

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        filesBtn.addActionListener(e -> openFilesWindow());

        JPanel bottom = new JPanel(new GridLayout(1,2));
        bottom.setBackground(bg);
        bottom.add(upload);
        bottom.add(filesBtn);

        add(bottom, BorderLayout.SOUTH);

        startDiscovery();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }

    //  ventana archivos
    private void openFilesWindow() {

        JFrame frame = new JFrame("Mis archivos");
        frame.setSize(300, 300);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bg);

        DefaultListModel<String> model = new DefaultListModel<>();

        for (String name : myFiles.keySet()) {
            model.addElement(name);
        }

        JList<String> list = new JList<>(model);
        list.setBackground(logBg);
        list.setForeground(green);

        JButton download = new JButton("Descargar");
        styleButton(download);

        download.addActionListener(e -> {

            String selected = list.getSelectedValue();

            if (selected != null) {

                log("📥 Descargando: " + selected);

                node.downloadFromNetwork();

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);

                        byte[] encryptedData =
                                FileAssembler.assembleToBytes(node.getChunks());

                        byte[] keyBytes = fileKeys.get(selected);

                        SecretKey key =
                                CryptoUtils.fromBytes(keyBytes);

                        byte[] decrypted =
                                CryptoUtils.decrypt(encryptedData, key);

                        Files.write(
                                Paths.get("reconstruido_" + selected),
                                decrypted
                        );

                        log("🔓 Archivo reconstruido");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        frame.add(new JScrollPane(list), BorderLayout.CENTER);
        frame.add(download, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void startDiscovery() {

    log("🚀 Nodo en puerto " + myPort);

    PeerDiscovery.startListening(myPort);
    PeerDiscovery.startBroadcast(myPort);

    //  HEARTBEAT → para el monitor
    new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(3000);

                com.dynamicbitmap.network.NetworkEventSender
                        .send("NODE:" + node.getId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();

    // 🔍 detección de peers
    new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(1500);

                for (String peer : PeerDiscovery.getPeers()) {

                    if (shownPeers.add(peer)) {

                        log("🔗 Nodo detectado: " + peer);

                        String[] parts = peer.split(":");
                        String host = parts[0];
                        int port = Integer.parseInt(parts[1]);

                        node.addNeighbor(new NodeInfo(host, port));

                        //  CLAVE: replicar inmediatamente al detectar nodo nuevo
                        new Thread(() -> {
                            try {
                                Thread.sleep(500); // pequeña espera
                                node.smartReplicate();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();

    //  SINCRONIZACIÓN CONTINUA (MUY IMPORTANTE)
    node.startAutoSync(3000);
}

    private void styleButton(JButton btn) {
        btn.setBackground(bg);
        btn.setForeground(green);
        btn.setFocusPainted(false);
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> log.append(msg + "\n"));
    }
}