package com.dynamicbitmap.monitor;

import com.dynamicbitmap.network.NodeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class NetworkMonitor {

    private List<NodeInfo> nodes;

    public NetworkMonitor(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public void start() {

        new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(2000);

                    System.out.println("\n===== ESTADO DE LA RED =====");

                    for (NodeInfo n : nodes) {
                        String status = requestStatus(n.host, n.port);
                        System.out.println(status);
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
            out.writeInt(4); //  tipo STATUS
            return in.readUTF();

        } catch (Exception e) {
            return "Nodo en " + port + " no responde";
        }
    }
}