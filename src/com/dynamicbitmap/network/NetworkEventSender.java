package com.dynamicbitmap.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkEventSender {

    private static final int MONITOR_PORT = 9999;

    public static void send(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();

            byte[] data = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("localhost"), // luego puede ser IP real
                    MONITOR_PORT
            );

            socket.send(packet);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}