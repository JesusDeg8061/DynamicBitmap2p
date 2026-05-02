package com.dynamicbitmap.ui;

import com.dynamicbitmap.network.NodeInfo;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.*;

public class NetworkUI extends JFrame {

    private java.util.List<NodeInfo> nodes = Arrays.asList(
            new NodeInfo("localhost", 5000),
            new NodeInfo("localhost", 5001),
            new NodeInfo("localhost", 5002)
    );

    private Map<Integer, JProgressBar> progressBars = new HashMap<>();
    private Map<Integer, JLabel> labels = new HashMap<>();

    public NetworkUI() {

        setTitle("DynamicBitmap Monitor");
        setSize(400, 250);
        setLayout(new GridLayout(nodes.size(), 1));
        getContentPane().setBackground(new Color(30, 30, 30));

        for (NodeInfo n : nodes) {

            JLabel label = new JLabel("Nodo " + n.port);
            label.setForeground(new Color(0, 255, 150));

            JProgressBar bar = new JProgressBar(0, 10);
            bar.setValue(0);
            bar.setForeground(new Color(0, 255, 150));
            bar.setBackground(Color.DARK_GRAY);

            progressBars.put(n.port, bar);
            labels.put(n.port, label);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBackground(new Color(30, 30, 30));

            panel.add(label, BorderLayout.NORTH);
            panel.add(bar, BorderLayout.CENTER);

            add(panel);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        startMonitoring();
    }

    private void startMonitoring() {

        new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(1000);

                    for (NodeInfo n : nodes) {
                        String status = requestStatus(n.host, n.port);

                        if (status != null) {
                            updateUI(n.port, status);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private String requestStatus(String host, int port) {
        try (
            Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            out.writeInt(4);
            return in.readUTF();

        } catch (Exception e) {
            return null;
        }
    }

    private void updateUI(int port, String status) {

        SwingUtilities.invokeLater(() -> {

            try {
                String[] parts = status.split("\\|");
                int chunks = Integer.parseInt(parts[1].split(":")[1].trim());

                progressBars.get(port).setValue(chunks);
                labels.get(port).setText("Nodo " + port + " | " + chunks + " chunks");

            } catch (Exception e) {
                labels.get(port).setText("Nodo " + port + " ERROR");
            }

        });
    }

    public static void main(String[] args) {
        new NetworkUI();
    }
}