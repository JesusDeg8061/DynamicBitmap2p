package com.dynamicbitmap.simulation;

import com.dynamicbitmap.node.Node;
import java.util.ArrayList;
import java.util.List;

public class NetworkSimulator {

    static final int SIZE = 10;
    static final int TARGET_REPLICAS = 2; // mínimo de copias por chunk

    public static void main(String[] args) {

        Node A = new Node("A", SIZE);
        Node B = new Node("B", SIZE);
        Node C = new Node("C", SIZE);

        // Estado inicial
        A.storeChunk(1, new byte[]{1});
        A.storeChunk(3, new byte[]{1});

        B.storeChunk(3, new byte[]{1});
        B.storeChunk(5, new byte[]{1});

        // C empieza vacío

        List<Node> network = new ArrayList<>();
        network.add(A);
        network.add(B);
        network.add(C);

        System.out.println("=== ANTES ===");
        printNetwork(network);

        //  Autorreparación
        selfHeal(network, SIZE, TARGET_REPLICAS);

        System.out.println("\n=== DESPUÉS (self-heal) ===");
        printNetwork(network);

        //  Balance (elimina exceso)
        balance(network, SIZE, TARGET_REPLICAS);

        System.out.println("\n=== DESPUÉS (balance) ===");
        printNetwork(network);
    }

    // Cuenta cuántas copias hay de un chunk
    static int countReplicas(int index, List<Node> network) {
        int count = 0;
        for (Node n : network) {
            if (n.hasChunk(index)) count++;
        }
        return count;
    }

    // Encuentra un nodo que tenga el chunk
    static Node findSource(int index, List<Node> network) {
        for (Node n : network) {
            if (n.hasChunk(index)) return n;
        }
        return null;
    }

    //  Autorreparación
    static void selfHeal(List<Node> network, int size, int target) {

        for (int i = 0; i < size; i++) {

            int replicas = countReplicas(i, network);

            if (replicas > 0 && replicas < target) {

                Node source = findSource(i, network);
                byte[] data = source.getChunk(i);

                Node bestNode = null;

                for (Node n : network) {
                    if (!n.hasChunk(i)) {
                        if (bestNode == null || n.chunkCount() < bestNode.chunkCount()) {
                            bestNode = n;
                        }
                    }
                }

                if (bestNode != null) {
                    bestNode.receiveChunk(i, data);
                }
            }
        }
    }

    //  Balanceo (elimina exceso de copias)
    static void balance(List<Node> network, int size, int target) {

        for (int i = 0; i < size; i++) {

            int replicas = countReplicas(i, network);

            if (replicas > target) {

                // ordenar por nodos más cargados primero
                network.sort((a, b) -> b.chunkCount() - a.chunkCount());

                for (Node n : network) {
                    if (n.hasChunk(i)) {

                        if (n.chunkCount() > 1) {
                            n.removeChunk(i);
                            replicas--;

                            if (replicas <= target) break;
                        }
                    }
                }
            }
        }
    }

    // Imprime estado de la red
    static void printNetwork(List<Node> network) {
        for (Node n : network) {
            System.out.println(
                "Nodo " + n.getId() +
                " | bitmap: " + n.getBitmap() +
                " | chunks: " + n.chunkCount()
            );
        }
    }
}