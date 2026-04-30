package com.dynamicbitmap.network;

import com.dynamicbitmap.node.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer implements Runnable {

    private int port;
    private Node node;

    public NodeServer(int port, Node node) {
        this.port = port;
        this.node = node;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Nodo " + node.getId() + " escuchando en puerto " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(() -> handleConnection(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {

    try (
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())
    ) {

        int type = in.readInt();

        // petición de bitmap
        if (type == 1) {
            String bitmapStr = node.getBitmap().toString();
            out.writeUTF(bitmapStr);
        }

        // recepción de chunk
        else if (type == 2) {
            int index = in.readInt();
            int length = in.readInt();

            byte[] data = new byte[length];
            in.readFully(data);

            node.receiveChunk(index, data);

            System.out.println("Nodo " + node.getId() + " recibio chunk " + index);
        }

        // petición de chunk
        else if (type == 3) {
            int index = in.readInt();
            byte[] data = node.getChunk(index);

            if (data != null) {
                out.writeInt(data.length);
                out.write(data);
            } else {
                out.writeInt(0);
            }

            out.flush();
        }
        
        else if (type == 4) {
    String status = node.getStatus();
    out.writeUTF(status);
}

    } catch (IOException e) {
        e.printStackTrace();
    } finally {   //  AQUÍ VA
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
}