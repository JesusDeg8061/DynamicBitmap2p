package com.dynamicbitmap.network;

import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class PeerDiscovery {

    private static final int PORT = 8888;

    //  evitar duplicados
    private static final Set<String> peers = new HashSet<>();

    // 🔍 escuchar nodos
    public static void startListening(int myPort) {

        new Thread(() -> {
            try {

                //  PERMITE múltiples instancias en mismo puerto
                DatagramSocket socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(PORT));

                byte[] buffer = new byte[256];

                while (true) {

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());

                    if (msg.startsWith("NODE:")) {

                        String peerPort = msg.split(":")[1];
                        String peerIP = packet.getAddress().getHostAddress();

                        // evitar detectarse a sí mismo
                        if (!peerPort.equals(String.valueOf(myPort))) {

                            String fullPeer = peerIP + ":" + peerPort;

                            //  evitar duplicados
                            if (!peers.contains(fullPeer)) {
                                peers.add(fullPeer);

                                System.out.println("🔗 Nodo encontrado: " + fullPeer);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 📡 anunciarse
    public static void startBroadcast(int myPort) {

        new Thread(() -> {
            try {

                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                while (true) {

                    String msg = "NODE:" + myPort;
                    byte[] data = msg.getBytes();

                    DatagramPacket packet = new DatagramPacket(
                            data,
                            data.length,
                            InetAddress.getByName("255.255.255.255"),
                            PORT
                    );

                    socket.send(packet);

                    Thread.sleep(2000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static Set<String> getPeers() {
        return peers;
    }
}