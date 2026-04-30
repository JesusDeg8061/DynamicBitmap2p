package com.dynamicbitmap.ui;

import com.dynamicbitmap.network.PeerDiscovery;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class MainUI extends JFrame {

    private int myPort;
    private JTextArea log;

    // 🔥 guardar peers ya mostrados
    private Set<String> shownPeers = new HashSet<>();

    public MainUI(String id) {

        setTitle("Nodo - " + id);
        setSize(500, 350);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30,30,30));

        // 🔥 puerto dinámico
        myPort = 5000 + (int)(Math.random() * 1000);

        // 🟢 header
        JLabel label = new JLabel("ID: " + id + " | Puerto: " + myPort);
        label.setForeground(new Color(0,255,150));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        add(label, BorderLayout.NORTH);

        // 🖥️ log
        log = new JTextArea();
        log.setBackground(new Color(20,20,20));
        log.setForeground(new Color(0,255,150));
        log.setEditable(false);

        JScrollPane scroll = new JScrollPane(log);
        add(scroll, BorderLayout.CENTER);

        // 🚀 iniciar sistema
        startDiscovery();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void startDiscovery() {

        log("🚀 Nodo iniciado en puerto " + myPort);

        PeerDiscovery.startListening(myPort);
        PeerDiscovery.startBroadcast(myPort);

        // 🔥 hilo limpio para nuevos peers
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1500);

                    for (String peer : PeerDiscovery.getPeers()) {

                        // ✔ SOLO mostrar nuevos
                        if (shownPeers.add(peer)) {
                            log("🔗 Nuevo nodo: " + peer);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
        });
    }
}