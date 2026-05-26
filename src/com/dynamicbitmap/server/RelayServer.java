package com.dynamicbitmap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelayServer {

    private static final int PORT = 9090;

    private static final Map<String, ClientConnection> clients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println(
                    "RelayServer iniciado en puerto "
                            + PORT
            );

            while (true) {

                Socket socket =
                        serverSocket.accept();

                new Thread(() ->
                        handleClient(socket)
                ).start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {

        String nodeId = null;

        try {

            DataInputStream in =
                    new DataInputStream(
                            socket.getInputStream()
                    );

            DataOutputStream out =
                    new DataOutputStream(
                            socket.getOutputStream()
                    );

            int type =
                    in.readInt();

            if (type != 100) {

                socket.close();

                return;
            }

            nodeId =
                    in.readUTF();

            ClientConnection connection =
                    new ClientConnection(
                            socket,
                            in,
                            out
                    );

            clients.put(
                    nodeId,
                    connection
            );

            System.out.println(
                    "Nodo conectado al relay: "
                            + nodeId
            );

            while (true) {

                int messageType =
                        in.readInt();

                if (messageType == 101) {

                    String targetNodeId =
                            in.readUTF();

                    int payloadLength =
                            in.readInt();

                    byte[] payload =
                            new byte[payloadLength];

                    in.readFully(payload);

                    forwardMessage(
                            nodeId,
                            targetNodeId,
                            payload
                    );
                }
            }

        } catch (Exception e) {

            if (nodeId != null) {

                clients.remove(nodeId);

                System.out.println(
                        "Nodo desconectado del relay: "
                                + nodeId
                );
            }

            try {

                socket.close();

            } catch (Exception ignored) {
            }
        }
    }

    private static void forwardMessage(
            String fromNodeId,
            String targetNodeId,
            byte[] payload
    ) {

        try {

            ClientConnection target =
                    clients.get(targetNodeId);

            if (target == null) {

                System.out.println(
                        "Destino no conectado al relay: "
                                + targetNodeId
                );

                return;
            }

            synchronized (target.out) {

                target.out.writeInt(102);

                target.out.writeUTF(fromNodeId);

                target.out.writeInt(payload.length);

                target.out.write(payload);

                target.out.flush();
            }

            System.out.println(
                    "Relay: "
                            + fromNodeId
                            + " -> "
                            + targetNodeId
                            + " | "
                            + payload.length
                            + " bytes"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static class ClientConnection {

        Socket socket;

        DataInputStream in;

        DataOutputStream out;

        ClientConnection(
                Socket socket,
                DataInputStream in,
                DataOutputStream out
        ) {

            this.socket = socket;

            this.in = in;

            this.out = out;
        }
    }
}