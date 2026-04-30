package com.dynamicbitmap.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NetworkGraphUI extends JPanel {

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
            progress += 0.02;
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

    private List<Node> nodes = new ArrayList<>();
    private List<Packet> packets = new ArrayList<>();

    public NetworkGraphUI() {

        setBackground(new Color(30, 30, 30));

        // posiciones de nodos
        nodes.add(new Node("A", 100, 150));
        nodes.add(new Node("B", 300, 80));
        nodes.add(new Node("C", 500, 150));

        // timer de animación
        Timer timer = new Timer(30, e -> {
            updatePackets();
            repaint();
        });
        timer.start();

        // generar tráfico
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(500);

                    Node a = nodes.get((int)(Math.random()*nodes.size()));
                    Node b = nodes.get((int)(Math.random()*nodes.size()));

                    if (a != b) {
                        packets.add(new Packet(a, b));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

        //  dibujar conexiones
        g2.setColor(Color.DARK_GRAY);
        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 != n2) {
                    g2.drawLine(n1.x, n1.y, n2.x, n2.y);
                }
            }
        }

        // 📦 dibujar paquetes
        g2.setColor(new Color(0, 255, 150));
        for (Packet p : packets) {
            g2.fillOval(p.getX(), p.getY(), 6, 6);
        }

        // 🟢 dibujar nodos
        for (Node n : nodes) {
            g2.setColor(new Color(0, 255, 150));
            g2.fillOval(n.x - 15, n.y - 15, 30, 30);

            g2.setColor(Color.BLACK);
            g2.drawString(n.name, n.x - 5, n.y + 5);
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Network Animation");
        frame.setSize(650, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new NetworkGraphUI());
        frame.setVisible(true);
    }
}