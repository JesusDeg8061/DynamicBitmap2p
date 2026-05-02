package com.dynamicbitmap.ui;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.List;

public class NetworkMonitorUI extends JPanel {

    static class Node {
        String id;
        int x, y;
        int sent = 0;
        int received = 0;

        long lastActive = System.currentTimeMillis();

        //  velocidad
        int lastSent = 0;
        int lastReceived = 0;
        int speedSend = 0;
        int speedReceive = 0;

        public Node(String id) {
            this.id = id;
        }

        boolean isActive() {
            return System.currentTimeMillis() - lastActive < 2000;
        }

        void updateSpeed() {
            speedSend = sent - lastSent;
            speedReceive = received - lastReceived;

            lastSent = sent;
            lastReceived = received;
        }
    }

    static class Packet {
        Node from, to;
        double progress = 0;
        boolean isSend;

        //  tamaño simulado del chunk
        int size;

        public Packet(Node from, Node to, boolean isSend) {
            this.from = from;
            this.to = to;
            this.isSend = isSend;

            //  tamaño aleatorio simulando chunk real
            this.size = 8 + new Random().nextInt(12);
        }

        public void update() {
            progress += 0.025;
        }

        public boolean done() {
            return progress >= 1;
        }

        int getX() {
            return (int)(from.x + (to.x - from.x) * progress);
        }

        int getY() {
            return (int)(from.y + (to.y - from.y) * progress);
        }
    }

    private Map<String, Node> nodes = new HashMap<>();
    private List<Packet> packets = new ArrayList<>();
    private Map<String, Long> activeLinks = new HashMap<>();

    public NetworkMonitorUI() {

        setBackground(new Color(20,20,20));

        //  animación
        new javax.swing.Timer(30, e -> {
            update();
            repaint();
        }).start();

        //  cálculo de velocidad cada segundo
        new javax.swing.Timer(1000, e -> {
            for (Node n : nodes.values()) {
                n.updateSpeed();
            }
        }).start();

        startListener();
    }

    //  ESCUCHA
    private void startListener() {

        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(9999);
                byte[] buffer = new byte[256];

                while (true) {

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());

                    processEvent(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //  EVENTOS
    private void processEvent(String msg) {

        SwingUtilities.invokeLater(() -> {

            try {

                String[] parts = msg.split(":");

                if (parts[0].equals("NODE")) {
                    nodes.computeIfAbsent(parts[1], Node::new);
                    layoutNodes();
                    return;
                }

                if (parts.length < 3) return;

                String type = parts[0];
                String fromId = parts[1];
                String toId = parts[2];

                Node from = nodes.computeIfAbsent(fromId, Node::new);
                Node to = nodes.computeIfAbsent(toId, Node::new);

                from.lastActive = System.currentTimeMillis();
                to.lastActive = System.currentTimeMillis();

                String link = fromId + "->" + toId;
                activeLinks.put(link, System.currentTimeMillis());

                if (type.equals("SEND")) {
                    from.sent++;
                    packets.add(new Packet(from, to, true));
                } else {
                    to.received++;
                    packets.add(new Packet(from, to, false));
                }

                layoutNodes();

            } catch (Exception ignored) {}
        });
    }

    //  POSICIÓN CIRCULAR
    private void layoutNodes() {

        int size = nodes.size();
        int i = 0;

        int cx = getWidth() > 0 ? getWidth()/2 : 350;
        int cy = getHeight() > 0 ? getHeight()/2 : 200;
        int radius = 160;

        for (Node n : nodes.values()) {

            double angle = 2 * Math.PI * i / Math.max(size,1);

            n.x = (int)(cx + radius * Math.cos(angle));
            n.y = (int)(cy + radius * Math.sin(angle));

            i++;
        }
    }

private void update() {

    packets.removeIf(p -> {
        p.update();
        return p.done();
    });

    //  limpiar conexiones viejas
    activeLinks.entrySet().removeIf(e ->
        System.currentTimeMillis() - e.getValue() > 2000
    );

    //  ELIMINAR NODOS INACTIVOS
    nodes.entrySet().removeIf(e ->
        System.currentTimeMillis() - e.getValue().lastActive > 5000
    );
}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //  conexiones
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(new Color(120,120,120));

        for (String link : activeLinks.keySet()) {
            String[] p = link.split("->");
            Node a = nodes.get(p[0]);
            Node b = nodes.get(p[1]);

            if (a != null && b != null) {
                g2.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        //  paquetes (chunks)
        for (Packet p : packets) {

            // glow
            g2.setColor(new Color(0,255,150,60));
            g2.fillOval(p.getX()-6, p.getY()-6, p.size+12, p.size+12);

            if (p.isSend) {
                g2.setColor(new Color(0,255,150));
            } else {
                g2.setColor(new Color(0,180,255));
            }

            g2.fillOval(p.getX(), p.getY(), p.size, p.size);
        }

        //  nodos
        for (Node n : nodes.values()) {

            if (n.isActive()) {
                g2.setColor(new Color(0,255,150));
            } else {
                g2.setColor(new Color(100,100,100));
            }

            g2.fillOval(n.x - 25, n.y - 25, 50, 50);

            // texto
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(n.id);
            g2.drawString(n.id, n.x - w/2, n.y);

            // stats + velocidad
            g2.setColor(new Color(180,180,180));
            g2.drawString(
                "↑" + n.sent + " ↓" + n.received +
                " | " + n.speedSend + "↑/s " + n.speedReceive + "↓/s",
                n.x - 45, n.y + 30
            );
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("ADMIN Monitor");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new NetworkMonitorUI());
        frame.setVisible(true);
    }
}