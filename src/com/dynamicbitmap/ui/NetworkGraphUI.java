package com.dynamicbitmap.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetworkGraphUI extends JPanel {

    public static NetworkGraphUI instance;

    static class Node {
        int x, y;
        String name;

        public Node(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    static class Packet {
        Node from, to;
        double progress = 0;

        public Packet(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        public void update() {
            progress += 0.03;
        }

        public boolean isDone() {
            return progress >= 1;
        }

        public int getX() {
            return (int) (from.x + (to.x - from.x) * progress);
        }

        public int getY() {
            return (int) (from.y + (to.y - from.y) * progress);
        }
    }

    private Map<String, Node> nodeMap = new HashMap<>();
    private List<Packet> packets = new ArrayList<>();

    public NetworkGraphUI() {

        instance = this;

        setBackground(new Color(30,30,30));

        //  timer de render
        new javax.swing.Timer(30, e -> {
            updatePackets();
            repaint();
        }).start();
    }

    //  CREA NODOS DINÁMICOS (FIX POSICIÓN)
    private Node getOrCreateNode(String id) {

        if (!nodeMap.containsKey(id)) {

            int index = nodeMap.size();

            //  FIX: usar tamaño fijo si aún no está renderizado
            int width = getWidth() > 0 ? getWidth() : 700;
            int height = getHeight() > 0 ? getHeight() : 300;

            int centerX = width / 2;
            int centerY = height / 2;
            int radius = 120;

            double angle = 2 * Math.PI * index / 8;

            int x = (int)(centerX + radius * Math.cos(angle));
            int y = (int)(centerY + radius * Math.sin(angle));

            Node node = new Node(id, x, y);

            nodeMap.put(id, node);

            //  FORZAR REDIBUJO
            repaint();
        }

        return nodeMap.get(id);
    }

    //  MÉTODO DE TRÁFICO (THREAD SAFE)
    public static void sendPacket(String fromId, String toId) {

        if (instance == null) return;

        SwingUtilities.invokeLater(() -> {

            Node from = instance.getOrCreateNode(fromId);
            Node to = instance.getOrCreateNode(toId);

            instance.packets.add(new Packet(from, to));
        });
    }

    private void updatePackets() {
        packets.removeIf(p -> {
            p.update();
            return p.isDone();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        //  conexiones
        g2.setColor(new Color(60,60,60));
        for (Node n1 : nodeMap.values()) {
            for (Node n2 : nodeMap.values()) {
                if (n1 != n2) {
                    g2.drawLine(n1.x, n1.y, n2.x, n2.y);
                }
            }
        }

        //  paquetes
        g2.setColor(new Color(0,255,150));
        for (Packet p : packets) {
            g2.fillOval(p.getX(), p.getY(), 10, 10);
        }

        //  nodos
        for (Node n : nodeMap.values()) {
            g2.setColor(new Color(0,255,150));
            g2.fillOval(n.x - 18, n.y - 18, 36, 36);

            g2.setColor(Color.BLACK);
            g2.drawString(n.name, n.x - 15, n.y + 5);
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("DynamicBitmap Monitor");
        frame.setSize(700, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new NetworkGraphUI());
        frame.setVisible(true);

        
    }
}