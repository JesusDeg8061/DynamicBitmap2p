package com.dynamicbitmap.simulation;

import com.dynamicbitmap.node.Node;
import com.dynamicbitmap.network.NodeServer;
import com.dynamicbitmap.network.NodeInfo;
import com.dynamicbitmap.core.FileChunker;
import com.dynamicbitmap.core.FileAssembler;
import com.dynamicbitmap.monitor.NetworkMonitor;

import java.util.List;
import java.util.Arrays;

public class NetworkTestReal {

    public static void main(String[] args) throws InterruptedException {

        Node A = new Node("A", 100);
        Node B = new Node("B", 100);
        Node C = new Node("C", 100);

        try {
            List<byte[]> chunks = FileChunker.splitFile("archivo.txt", 1024);

            // 🔥 DISTRIBUCIÓN REAL
            for (int i = 0; i < chunks.size(); i++) {

                if (i % 3 == 0) {
                    A.storeChunk(i, chunks.get(i));
                } else if (i % 3 == 1) {
                    B.storeChunk(i, chunks.get(i));
                } else {
                    C.storeChunk(i, chunks.get(i));
                }
            }

            System.out.println("Archivo distribuido en la red con " + chunks.size() + " chunks");

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 🚀 SERVIDORES
        new Thread(new NodeServer(5000, A)).start();
        new Thread(new NodeServer(5001, B)).start();
        new Thread(new NodeServer(5002, C)).start();

        Thread.sleep(2000);

        // 🔗 CONEXIONES
        A.addNeighbor(new NodeInfo("localhost", 5001));

        B.addNeighbor(new NodeInfo("localhost", 5000));
        B.addNeighbor(new NodeInfo("localhost", 5002));

        C.addNeighbor(new NodeInfo("localhost", 5001));
        C.addNeighbor(new NodeInfo("localhost", 5000));

        // 🔄 RED VIVA
        A.startAutoSync(2000);
        B.startAutoSync(2000);
        C.startAutoSync(2000);

        // 📊 MONITOR EN VIVO
        NetworkMonitor monitor = new NetworkMonitor(
            Arrays.asList(
                new NodeInfo("localhost", 5000),
                new NodeInfo("localhost", 5001),
                new NodeInfo("localhost", 5002)
            )
        );
        monitor.start();

        //  TIEMPO DE SIMULACIÓN
        Thread.sleep(20000);

        System.out.println("\n=== ESTADO FINAL ===");
        System.out.println("A: " + A.getBitmap());
        System.out.println("B: " + B.getBitmap());
        System.out.println("C: " + C.getBitmap());

        //  RECONSTRUCCIÓN
        FileAssembler.assemble(C.getChunks(), "reconstruido.txt");
    }
}