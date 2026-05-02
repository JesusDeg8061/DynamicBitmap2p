    package com.dynamicbitmap.node;

    import com.dynamicbitmap.core.DynamicBitmap;
    import java.util.HashMap;
    import java.util.Map;
    import com.dynamicbitmap.network.NodeInfo;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.Collections;
    import com.dynamicbitmap.network.NetworkEventSender;

    import java.io.DataOutputStream;
    import java.io.DataInputStream;
    import java.net.Socket;

    public class Node {

        private String id;
        private DynamicBitmap bitmap;
        private Map<Integer, byte[]> chunks;

        private List<NodeInfo> neighbors = Collections.synchronizedList(new ArrayList<>());

        private Map<String, String> lastKnownBitmaps = new HashMap<>();
        private Map<String, Long> lastSyncTime = new HashMap<>();
        private volatile boolean running = true;

        private ExecutorService executor = Executors.newFixedThreadPool(8);
        private long fileSize = 0;


        public Node(String id, int size) {
            this.id = id;
            this.bitmap = new DynamicBitmap(size);
            this.chunks = new HashMap<>();
        }

        public void storeChunk(int index, byte[] data) {
            chunks.put(index, data);
            bitmap.set(index);
        }

        public boolean hasChunk(int index) {
            return bitmap.get(index);
        }

        public byte[] getChunk(int index) {
            return chunks.get(index);
        }

        public void receiveChunk(int index, byte[] data) {
            if (!hasChunk(index) && data != null) {
                storeChunk(index, data);
            }
        }

        public int chunkCount() {
            return chunks.size();
        }

        public DynamicBitmap getBitmap() {
            return bitmap;
        }

        public String getId() {
            return id;
        }


        public Map<Integer, byte[]> getChunks() {
            return chunks;
        }

        public void removeChunk(int index) {
            if (hasChunk(index)) {
                chunks.remove(index);
                bitmap.clear(index);
            }
        }

        //  enviar chunk
       public void sendChunk(String host, int port, int index) {
        try (
            Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            byte[] data = getChunk(index);
            if (data == null) return;

            out.writeInt(2);
            out.writeInt(index);
            out.writeInt(data.length);
            out.write(data);

            System.out.println("Nodo " + id + " envio chunk " + index + " a puerto " + port);

            NetworkEventSender.send("SEND:" + id + ":" + port);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public void sendChunkAsync(String host, int port, int index) {
            executor.submit(() -> sendChunk(host, port, index));
        }

        //  pedir bitmap
        public String requestBitmap(String host, int port) {
            try (
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
            ) {
                out.writeInt(1);
                return in.readUTF();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        //  pedir chunk
        public byte[] requestChunk(String host, int port, int index) {
            try (
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
            ) {
                out.writeInt(3);
                out.writeInt(index);

                int length = in.readInt();
                if (length <= 0) return null;

                byte[] data = new byte[length];
                in.readFully(data);
                return data;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void addNeighbor(NodeInfo neighbor) {
            neighbors.add(neighbor);
        }

        //  replicación inteligente
        public void smartReplicate() {

    if (neighbors.isEmpty()) return;

    List<NodeInfo> copy = new ArrayList<>(neighbors);

    // 🔥 incluirme a mí también en la distribución
    List<NodeInfo> allNodes = new ArrayList<>(copy);

    int totalNodes = allNodes.size();

    long now = System.currentTimeMillis();

    for (Map.Entry<Integer, byte[]> entry : chunks.entrySet()) {

        int chunkId = entry.getKey();

        //  determinar dueño del chunk
        int ownerIndex = chunkId % totalNodes;

        NodeInfo target = allNodes.get(ownerIndex);

        
        String key = target.host + ":" + target.port;

        long last = lastSyncTime.getOrDefault(key, 0L);
        if (now - last < 3000) continue;

        lastSyncTime.put(key, now);

        System.out.println("Nodo " + id +
                " → enviando chunk " + chunkId +
                " a dueño " + target.port);

        sendChunkAsync(target.host, target.port, chunkId);
    }
}
        //  descarga multi-nodo
        public void downloadFromNetwork() {

        Map<NodeInfo, String> bitmapCache = new HashMap<>();
        Map<Integer, Integer> rarity = new HashMap<>();

        //  1. obtener bitmap UNA vez por nodo
        List<NodeInfo> neighborsCopy = new ArrayList<>(neighbors);
        for (NodeInfo n : neighbors) {
            String bitmap = requestBitmap(n.host, n.port);

            if (bitmap != null) {
                bitmapCache.put(n, bitmap);

                for (int i = 0; i < bitmap.length(); i++) {
                    if (bitmap.charAt(i) == '1') {
                        rarity.put(i, rarity.getOrDefault(i, 0) + 1);
                    }
                }
            }
        }

        //  2. ordenar chunks por rareza
        List<Integer> chunksToDownload = new ArrayList<>();

        for (int i = 0; i < bitmap.getSize(); i++) {
            if (!hasChunk(i)) {
                chunksToDownload.add(i);
            }
        }

        chunksToDownload.sort((a, b) ->
            Integer.compare(
                rarity.getOrDefault(a, Integer.MAX_VALUE),
                rarity.getOrDefault(b, Integer.MAX_VALUE)
            )
        );

        //  3. descargar
        for (int i : chunksToDownload) {

            List<NodeInfo> availableNodes = new ArrayList<>();

            for (NodeInfo n : bitmapCache.keySet()) {
                String bitmap = bitmapCache.get(n);

                if (i < bitmap.length() && bitmap.charAt(i) == '1') {
                    availableNodes.add(n);
                }
            }

            if (!availableNodes.isEmpty()) {

                NodeInfo selected = availableNodes.get(
                    (int) (Math.random() * availableNodes.size())
                );

                int chunkIndex = i;

                executor.submit(() -> {
                    byte[] data = requestChunk(selected.host, selected.port, chunkIndex);

                    if (data != null && data.length > 0 && !hasChunk(chunkIndex)) {
                        receiveChunk(chunkIndex, data);

                        System.out.println("Nodo " + id +
                                " descargo chunk " + chunkIndex +
                                " desde " + selected.port);

                        NetworkEventSender.send("RECEIVE:" + selected.port + ":" + id);
                    }
                });
            }
        }
    }
        //  auto sync
        public void startAutoSync(long intervalMs) {

            new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(intervalMs);

                        downloadFromNetwork(); //  IMPORTANTE

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        public String getStatus() {
        return "Nodo " + id +
               " | chunks: " + chunkCount() +
               " | bitmap: " + bitmap.toString();
    }

        public void stop() {
            running = false;
        }

        public void setFileSize(long size) {
        this.fileSize = size;
    }

    public long getFileSize() {
        return fileSize;
    }
    public long getCurrentSize() {

    long total = 0;

    for (byte[] chunk : chunks.values()) {
        if (chunk != null) {
            total += chunk.length;
        }
    }

    return total;
}
    }