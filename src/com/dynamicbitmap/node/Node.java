package com.dynamicbitmap.node;

import com.dynamicbitmap.core.DynamicBitmap;
import com.dynamicbitmap.network.NodeInfo;
import com.dynamicbitmap.network.NetworkEventSender;
import com.dynamicbitmap.network.RelayClient;
import com.dynamicbitmap.storage.ChunkStorage;
import com.dynamicbitmap.metadata.FileMetadata;
import com.dynamicbitmap.metadata.MetadataStorage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {

    private String id;

    private DynamicBitmap bitmap;

    // archivo principal temporal
    private static final String DEFAULT_FILE = "main";

    // MULTIARCHIVO
    private Map<String, Map<Integer, byte[]>> fileChunks =
            new ConcurrentHashMap<>();

    private Map<String, FileMetadata> files =
            new ConcurrentHashMap<>();

    private List<NodeInfo> neighbors =
            Collections.synchronizedList(new ArrayList<>());

    private Map<String, String> lastKnownBitmaps =
            new ConcurrentHashMap<>();

    private Map<String, Long> lastSyncTime =
            new ConcurrentHashMap<>();

    private volatile boolean running = true;

    private ExecutorService executor =
            Executors.newFixedThreadPool(4);

    private final Set<Integer> downloadingChunks =
            ConcurrentHashMap.newKeySet();

    private long fileSize = 0;

    private RelayClient relayClient;
    // RESPUESTAS RELAY
private Map<String, String> relayBitmapResponses =
        new ConcurrentHashMap<>();

private Map<String, byte[]> relayChunkResponses =
        new ConcurrentHashMap<>();

    public Node(String id, int size) {

        this.id = id;

        this.bitmap = new DynamicBitmap(size);

        // mapa inicial
        fileChunks.put(
                DEFAULT_FILE,
                new ConcurrentHashMap<>()
        );

        loadPersistedMetadata();

        loadPersistedChunks();
    }

    // CONFIGURAR RELAY
    public void setRelayClient(RelayClient relayClient) {

        this.relayClient = relayClient;
    }

    public void receiveRelayChunk(int index, byte[] data) {

    receiveChunk(index, data);

    relayChunkResponses.put(
            String.valueOf(index),
            data
    );
}

    // ENVIAR CHUNK POR RELAY
    public void sendChunkViaRelay(
            String targetNodeId,
            int index
    ) {

        if (relayClient == null) {

            System.out.println(
                    "RelayClient no configurado"
            );

            return;
        }

        if (targetNodeId == null || targetNodeId.isEmpty()) {

            System.out.println(
                    "No se puede enviar por relay: targetNodeId vacío"
            );

            return;
        }

        byte[] data =
                getChunk(index);

        if (data == null) {
            return;
        }

        byte[] payload =
                RelayClient.buildChunkPayload(
                        index,
                        data
                );

        if (payload == null) {
            return;
        }

        relayClient.send(
                targetNodeId,
                payload
        );

        System.out.println(
                "Nodo "
                        + id
                        + " envio chunk "
                        + index
                        + " por relay a "
                        + targetNodeId
        );
    }

    public void sendChunkViaRelayAsync(
            String targetNodeId,
            int index
    ) {

        executor.submit(() ->
                sendChunkViaRelay(
                        targetNodeId,
                        index
                )
        );
    }
    
    public String requestBitmapViaRelay(
        String targetNodeId
) {

    try {

        if (relayClient == null) {
            return null;
        }

        String request =
                "BITMAP_REQUEST";

        relayClient.send(
                targetNodeId,
                request.getBytes()
        );

        long start =
                System.currentTimeMillis();

        while (
                System.currentTimeMillis() - start
                        < 3000
        ) {

            String bitmap =
                    relayBitmapResponses.remove(
                            targetNodeId
                    );

            if (bitmap != null) {
                return bitmap;
            }

            Thread.sleep(50);
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return null;
}

public byte[] requestChunkViaRelay(
        String targetNodeId,
        int index
) {

    try {

        if (relayClient == null) {
            return null;
        }

        String request =
                "CHUNK_REQUEST:"
                        + index;

        relayClient.send(
                targetNodeId,
                request.getBytes()
        );

        long start =
                System.currentTimeMillis();

        while (
                System.currentTimeMillis() - start
                        < 5000
        ) {

            byte[] data =
                    relayChunkResponses.remove(
                            String.valueOf(index)
                    );

            if (data != null) {
                return data;
            }

            Thread.sleep(50);
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return null;
}

    // OBTENER MAPA ACTUAL
    private Map<Integer, byte[]> getMainChunks() {

        return fileChunks.get(DEFAULT_FILE);
    }

    // REGISTRAR ARCHIVO EN CATÁLOGO
    public void registerFile(FileMetadata metadata) {

        if (metadata == null) {
            return;
        }

        files.put(
                metadata.getFileId(),
                metadata
        );

        MetadataStorage.saveMetadata(
                id,
                metadata
        );
    }

    // OBTENER TODOS LOS ARCHIVOS DEL CATÁLOGO
    public Map<String, FileMetadata> getFiles() {

        return files;
    }

    // OBTENER METADATA DE UN ARCHIVO
    public FileMetadata getFileMetadata(String fileId) {

        return files.get(fileId);
    }

    // CARGAR CATÁLOGO PERSISTENTE
    private void loadPersistedMetadata() {

        List<FileMetadata> metadataList =
                MetadataStorage.loadAllMetadata(id);

        for (FileMetadata metadata : metadataList) {

            if (metadata != null) {

                files.put(
                        metadata.getFileId(),
                        metadata
                );

                System.out.println(
                        "Nodo "
                                + id
                                + " cargó metadata de archivo "
                                + metadata.getFileName()
                );
            }
        }
    }

    // GUARDAR CHUNK
    public void storeChunk(int index, byte[] data) {

        Map<Integer, byte[]> chunks =
                getMainChunks();

        if (chunks.containsKey(index)) {
            return;
        }

        chunks.put(index, data);

        bitmap.set(index);

        ChunkStorage.saveChunk(
                id,
                DEFAULT_FILE,
                index,
                data
        );

        System.out.println(
                "Nodo "
                        + id
                        + " guardó chunk "
                        + index
        );
    }

    public boolean hasChunk(int index) {

        return bitmap.get(index);
    }

    public byte[] getChunk(int index) {

        return getMainChunks().get(index);
    }

    public void receiveChunk(int index, byte[] data) {

        if (!hasChunk(index) && data != null) {

            storeChunk(index, data);
        }
    }

    public int chunkCount() {

        return getMainChunks().size();
    }

    public DynamicBitmap getBitmap() {

        return bitmap;
    }

    public String getId() {

        return id;
    }

    public Map<Integer, byte[]> getChunks() {

        return getMainChunks();
    }

public void removeChunk(int index) {

    if (hasChunk(index)) {

        getMainChunks().remove(index);

        bitmap.clear(index);

        ChunkStorage.deleteChunk(
                id,
                DEFAULT_FILE,
                index
        );
    }
}

    // ENVIAR CHUNK DIRECTO CON FALLBACK A RELAY
    public void sendChunk(
            NodeInfo target,
            int index
    ) {

        if (target == null) {
            return;
        }

        try (
                Socket socket =
                        new Socket(target.host, target.port);

                DataOutputStream out =
                        new DataOutputStream(
                                socket.getOutputStream()
                        )
        ) {

            byte[] data =
                    getChunk(index);

            if (data == null) {
                return;
            }

            out.writeInt(2);

            out.writeInt(index);

            out.writeInt(data.length);

            out.write(data);
            out.flush();

            System.out.println(
                    "Nodo "
                            + id
                            + " envio chunk "
                            + index
                            + " directo a puerto "
                            + target.port
            );

            NetworkEventSender.send(
                    "SEND:"
                            + id
                            + ":"
                            + target.port
            );

        } catch (Exception e) {

            System.out.println(
                    "No se pudo enviar chunk directo a "
                            + target.port
            );

            if (target.hasNodeId()) {

                System.out.println(
                        "Intentando enviar chunk "
                                + index
                                + " por relay a "
                                + target.nodeId
                );

                sendChunkViaRelay(
                        target.nodeId,
                        index
                );

            } else {

                neighbors.removeIf(n ->
                        n.host.equals(target.host)
                                &&
                                n.port == target.port
                );
            }
        }
    }

    // MÉTODO ORIGINAL CONSERVADO
    public void sendChunk(
            String host,
            int port,
            int index
    ) {

        sendChunk(
                new NodeInfo(
                        host,
                        port
                ),
                index
        );
    }

    public void sendChunkAsync(
            NodeInfo target,
            int index
    ) {

        executor.submit(() ->
                sendChunk(
                        target,
                        index
                )
        );
    }

    // MÉTODO ORIGINAL CONSERVADO
    public void sendChunkAsync(
            String host,
            int port,
            int index
    ) {

        executor.submit(() ->
                sendChunk(
                        host,
                        port,
                        index
                )
        );
    }

    // PEDIR BITMAP
public String requestBitmap(
        String host,
        int port
) {

    try (
            Socket socket =
                    new Socket(host, port);

            DataOutputStream out =
                    new DataOutputStream(
                            socket.getOutputStream()
                    );

            DataInputStream in =
                    new DataInputStream(
                            socket.getInputStream()
                    )
    ) {

        socket.setSoTimeout(3000);

        out.writeInt(1);

        return in.readUTF();

    } catch (Exception e) {

        NodeInfo relayNode = null;

        for (NodeInfo n : neighbors) {

            if (
                    n.host.equals(host)
                            &&
                            n.port == port
            ) {

                relayNode = n;
                break;
            }
        }

        if (
                relayNode != null
                        &&
                        relayNode.hasNodeId()
        ) {

            System.out.println(
                    "Bitmap directo falló. Intentando relay..."
            );

            return requestBitmapViaRelay(
                    relayNode.nodeId
            );
        }

        neighbors.removeIf(n ->
                n.host.equals(host)
                        &&
                        n.port == port
        );

        return null;
    }
}

    // PEDIR CHUNK
    public byte[] requestChunk(
            String host,
            int port,
            int index
    ) {

        try (
                Socket socket =
                        new Socket(host, port);

                DataOutputStream out =
                        new DataOutputStream(
                                socket.getOutputStream()
                        );

                DataInputStream in =
                        new DataInputStream(
                                socket.getInputStream()
                        )
        ) {

            out.writeInt(3);

            out.writeInt(index);

            int length = in.readInt();

            if (length <= 0) {
                return null;
            }

            byte[] data =
                    new byte[length];

            in.readFully(data);

            return data;

        } catch (Exception e) {

            neighbors.removeIf(n ->
                    n.host.equals(host)
                            &&
                            n.port == port
            );

            return null;
        }
    }

    public void addNeighbor(NodeInfo neighbor) {

        if (!neighbors.contains(neighbor)) {

            neighbors.add(neighbor);
        }
    }

    // DISTRIBUCIÓN REAL
    public void smartReplicate() {

    if (neighbors.isEmpty()) {
        return;
    }

    long now =
            System.currentTimeMillis();

    for (
            Map.Entry<Integer, byte[]> entry :
            getMainChunks().entrySet()
    ) {

        int chunkId =
                entry.getKey();

        NodeInfo target =
                getChunkOwner(
                        chunkId
                );

        if (target == null) {
            continue;
        }

        String key =
                target.host
                        + ":"
                        + target.port;

        long last =
                lastSyncTime.getOrDefault(
                        key,
                        0L
                );

        if (now - last < 3000) {
            continue;
        }

        lastSyncTime.put(
                key,
                now
        );

        System.out.println(
                "Nodo "
                        + id
                        + " enviando chunk "
                        + chunkId
                        + " a "
                        + target.nodeId
        );

        sendChunkAsync(
                target,
                chunkId
        );
    }
    rebalanceStorage();
}
    // DESCARGA DISTRIBUIDA
    public void downloadFromNetwork() {

        Map<NodeInfo, String> bitmapCache =
                new HashMap<>();

        Map<Integer, Integer> rarity =
                new HashMap<>();

        List<NodeInfo> neighborsCopy =
                new ArrayList<>(neighbors);

        for (NodeInfo n : neighborsCopy) {

            String bitmap =
                    requestBitmap(
                            n.host,
                            n.port
                    );

            if (bitmap != null) {

                bitmapCache.put(n, bitmap);

                for (
                        int i = 0;
                        i < bitmap.length();
                        i++
                ) {

                    if (bitmap.charAt(i) == '1') {

                        rarity.put(
                                i,
                                rarity.getOrDefault(i, 0) + 1
                        );
                    }
                }
            }
        }

        List<Integer> chunksToDownload =
                new ArrayList<>();

        for (
                int i = 0;
                i < bitmap.getSize();
                i++
        ) {

            if (!hasChunk(i)) {

                chunksToDownload.add(i);
            }
        }

        chunksToDownload.sort((a, b) ->

                Integer.compare(
                        rarity.getOrDefault(
                                a,
                                Integer.MAX_VALUE
                        ),
                        rarity.getOrDefault(
                                b,
                                Integer.MAX_VALUE
                        )
                )
        );

        for (int i : chunksToDownload) {

            if (
                    hasChunk(i)
                            ||
                            downloadingChunks.contains(i)
            ) {
                continue;
            }

            List<NodeInfo> availableNodes =
                    new ArrayList<>();

            for (NodeInfo n :
                    bitmapCache.keySet()) {

                String bitmap =
                        bitmapCache.get(n);

                if (
                        i < bitmap.length()
                                &&
                                bitmap.charAt(i) == '1'
                ) {

                    availableNodes.add(n);
                }
            }

            if (!availableNodes.isEmpty()) {

                NodeInfo selected =
                        availableNodes.get(
                                (int)
                                        (
                                                Math.random()
                                                        *
                                                        availableNodes.size()
                                        )
                        );

                int chunkIndex = i;

                downloadingChunks.add(chunkIndex);

                executor.submit(() -> {

                    try {

                        byte[] data =
                                requestChunk(
                                        selected.host,
                                        selected.port,
                                        chunkIndex
                                );

                        if (
                                data != null
                                        &&
                                        data.length > 0
                                        &&
                                        !hasChunk(chunkIndex)
                        ) {

                            receiveChunk(
                                    chunkIndex,
                                    data
                            );

                            System.out.println(
                                    "Nodo "
                                            + id
                                            + " descargó chunk "
                                            + chunkIndex
                                            + " desde "
                                            + selected.port
                            );

                            NetworkEventSender.send(
                                    "RECEIVE:"
                                            + selected.port
                                            + ":"
                                            + id
                            );
                        }

                    } finally {

                        downloadingChunks.remove(
                                chunkIndex
                        );
                    }
                });
            }
        }
    }

    // AUTO SYNC
    public void startAutoSync(long intervalMs) {

        new Thread(() -> {

            while (running) {

                try {

                    Thread.sleep(intervalMs);

                    downloadFromNetwork();
                    smartReplicate();

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getStatus() {

        return "Nodo "
                + id
                + " | chunks: "
                + chunkCount()
                + " | archivos: "
                + files.size()
                + " | bitmap: "
                + bitmap.toString();
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
    
    public long getSharedCapacity() {

    return bitmap.getSize();
}

    public long getCurrentSize() {

        long total = 0;

        for (
                byte[] chunk :
                getMainChunks().values()
        ) {

            if (chunk != null) {

                total += chunk.length;
            }
        }

        return total;
    }
    
public int getOnlineNodes() {

    return neighbors.size() + 1;
}
    
    public int getIdealChunkCount() {

    int totalNodes =
            getOnlineNodes();

    if (totalNodes <= 0) {
        return chunkCount();
    }

    return Math.max(
            1,
            chunkCount() / totalNodes
    );
}
    
    public boolean isOverloaded() {

    return chunkCount()
            > getIdealChunkCount();
}

    // RECUPERAR CHUNKS
    private void loadPersistedChunks() {

        File[] files =
                ChunkStorage.loadFileChunks(
                        id,
                        DEFAULT_FILE
                );

        if (files == null) {
            return;
        }

        for (File f : files) {

            try {

                String name =
                        f.getName()
                                .replace(
                                        ".chunk",
                                        ""
                                );

                int chunkId =
                        Integer.parseInt(name);

                if (hasChunk(chunkId)) {
                    continue;
                }

                byte[] data =
                        ChunkStorage.loadChunk(
                                id,
                                DEFAULT_FILE,
                                chunkId
                        );

                if (data != null) {

                    getMainChunks().put(
                            chunkId,
                            data
                    );

                    bitmap.set(chunkId);

                    System.out.println(
                            "Nodo "
                                    + id
                                    + " recuperó chunk "
                                    + chunkId
                    );
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
    
                public List<String> getAllNodeIds() {

                List<String> ids =
                        new ArrayList<>();

                ids.add(id);

                synchronized (neighbors) {

                    for (NodeInfo n : neighbors) {

                        if (
                                n.nodeId != null
                                &&
                                !n.nodeId.isEmpty()
                        ) {

                            ids.add(n.nodeId);
                        }
                    }
                }

                Collections.sort(ids);

                return ids;
            }
            public boolean shouldOwnChunk(
                int chunkId
        ) {

            List<String> nodes =
                    getAllNodeIds();

            if (nodes.isEmpty()) {
                return true;
            }

            int ownerIndex =
                    chunkId % nodes.size();

            return id.equals(
                    nodes.get(ownerIndex)
            );
        }
            
            public List<Integer> getChunksToMove() {

    List<Integer> chunksToMove =
            new ArrayList<>();

    for (
            Integer chunkId :
            getMainChunks().keySet()
    ) {

        if (
                !shouldOwnChunk(chunkId)
        ) {

            chunksToMove.add(
                    chunkId
            );
        }
    }

    return chunksToMove;
}

public int getChunksToMoveCount() {

    return getChunksToMove().size();
}

public boolean canMoveChunk(
        int chunkId
) {

    NodeInfo owner =
            getChunkOwner(
                    chunkId
            );

    if (owner == null) {
        return false;
    }

    return !shouldOwnChunk(
            chunkId
    );
}

public NodeInfo getChunkOwner(
        int chunkId
) {

    List<String> ids =
            getAllNodeIds();

    if (ids.isEmpty()) {
        return null;
    }

    int ownerIndex =
            chunkId % ids.size();

    String ownerId =
            ids.get(ownerIndex);

    synchronized (neighbors) {

        for (NodeInfo n : neighbors) {

            if (
                    ownerId.equals(
                            n.nodeId
                    )
            ) {

                return n;
            }
        }
    }

    return null;
}
public void printRebalancePlan() {

    List<Integer> chunks =
            getChunksToMove();

    for (
            Integer chunkId :
            chunks
    ) {

        NodeInfo owner =
                getChunkOwner(
                        chunkId
                );

        if (owner != null) {

            System.out.println(
                    "Chunk "
                            + chunkId
                            + " debería migrarse a "
                            + owner.nodeId
            );
        }
    }
}

public void rebalanceStorage() {

    for (
            Integer chunkId :
            getChunksToMove()
    ) {

        NodeInfo owner =
                getChunkOwner(
                        chunkId
                );

        if (owner == null) {
            continue;
        }

        System.out.println(
                "[REBALANCE] "
                        + chunkId
                        + " -> "
                        + owner.nodeId
        );

        try {

            sendChunk(
                    owner,
                    chunkId
            );

            Thread.sleep(500);

            if (
                    !shouldOwnChunk(
                            chunkId
                    )
            ) {

                removeChunk(
                        chunkId
                );

                System.out.println(
                        "[REBALANCE OK] Chunk "
                                + chunkId
                                + " eliminado localmente"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

public void clearNeighbors() {

    neighbors.clear();
}

public List<NodeInfo> getNeighbors() {

    synchronized (neighbors) {

        return new ArrayList<>(neighbors);
    }
}
}