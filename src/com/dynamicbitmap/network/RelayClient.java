package com.dynamicbitmap.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;

public class RelayClient {

    private static final String RELAY_HOST =
            "150.136.38.250";

    private static final int RELAY_PORT =
            9090;

    private String nodeId;

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private volatile boolean running =
            false;

    private RelayMessageHandler handler;

    public RelayClient(
            String nodeId
    ) {

        this.nodeId = nodeId;
    }

    public void connect(
            RelayMessageHandler handler
    ) {

        this.handler = handler;

        new Thread(() -> {

            while (true) {

                try {

                    socket =
                            new Socket(
                                    RELAY_HOST,
                                    RELAY_PORT
                            );

                    in =
                            new DataInputStream(
                                    socket.getInputStream()
                            );

                    out =
                            new DataOutputStream(
                                    socket.getOutputStream()
                            );

                    out.writeInt(100);

                    out.writeUTF(nodeId);

                    out.flush();

                    running = true;

                    System.out.println(
                            "Conectado al relay como "
                                    + nodeId
                    );

                    listenLoop();

                } catch (Exception e) {

                    running = false;

                    System.out.println(
                            "Relay no disponible, reintentando..."
                    );

                    try {

                        Thread.sleep(3000);

                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }).start();
    }

    private void listenLoop() throws Exception {

        while (running) {

            int type =
                    in.readInt();

            if (type == 102) {

                String fromNodeId =
                        in.readUTF();

                int length =
                        in.readInt();

                byte[] payload =
                        new byte[length];

                in.readFully(payload);

                if (handler != null) {

                    handler.onRelayMessage(
                            fromNodeId,
                            payload
                    );
                }
            }
        }
    }

    public void send(
            String targetNodeId,
            byte[] payload
    ) {

        try {

            if (
                    out == null
                            ||
                            socket == null
                            ||
                            socket.isClosed()
            ) {

                return;
            }

            synchronized (out) {

                out.writeInt(101);

                out.writeUTF(targetNodeId);

                out.writeInt(payload.length);

                out.write(payload);

                out.flush();
            }

        } catch (Exception e) {

            running = false;
        }
    }

    public static byte[] buildChunkPayload(
            int chunkIndex,
            byte[] data
    ) {

        try {

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            DataOutputStream dos =
                    new DataOutputStream(baos);

            dos.writeInt(200);

            dos.writeInt(chunkIndex);

            dos.writeInt(data.length);

            dos.write(data);

            dos.flush();

            return baos.toByteArray();

        } catch (Exception e) {

            return null;
        }
    }

    public static ChunkPayload parseChunkPayload(
            byte[] payload
    ) {

        try {

            DataInputStream dis =
                    new DataInputStream(
                            new ByteArrayInputStream(payload)
                    );

            int type =
                    dis.readInt();

            if (type != 200) {
                return null;
            }

            int chunkIndex =
                    dis.readInt();

            int length =
                    dis.readInt();

            byte[] data =
                    new byte[length];

            dis.readFully(data);

            return new ChunkPayload(
                    chunkIndex,
                    data
            );

        } catch (Exception e) {

            return null;
        }
    }
    
    public static byte[] buildBitmapRequestPayload() {

    try {

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        DataOutputStream dos =
                new DataOutputStream(baos);

        dos.writeInt(201);

        dos.flush();

        return baos.toByteArray();

    } catch (Exception e) {

        return null;
    }
}

public static boolean isBitmapRequest(
        byte[] payload
) {

    try {

        DataInputStream dis =
                new DataInputStream(
                        new ByteArrayInputStream(payload)
                );

        int type =
                dis.readInt();

        return type == 201;

    } catch (Exception e) {

        return false;
    }
}

public static byte[] buildChunkRequestPayload(
        int chunkIndex
) {

    try {

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        DataOutputStream dos =
                new DataOutputStream(baos);

        dos.writeInt(202);

        dos.writeInt(chunkIndex);

        dos.flush();

        return baos.toByteArray();

    } catch (Exception e) {

        return null;
    }
}

public static Integer parseChunkRequestPayload(
        byte[] payload
) {

    try {

        DataInputStream dis =
                new DataInputStream(
                        new ByteArrayInputStream(payload)
                );

        int type =
                dis.readInt();

        if (type != 202) {
            return null;
        }

        return dis.readInt();

    } catch (Exception e) {

        return null;
    }
}

public static byte[] buildBitmapResponsePayload(
        String bitmap
) {

    try {

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        DataOutputStream dos =
                new DataOutputStream(baos);

        dos.writeInt(203);

        dos.writeUTF(bitmap);

        dos.flush();

        return baos.toByteArray();

    } catch (Exception e) {

        return null;
    }
}

public static String parseBitmapResponsePayload(
        byte[] payload
) {

    try {

        DataInputStream dis =
                new DataInputStream(
                        new ByteArrayInputStream(payload)
                );

        int type =
                dis.readInt();

        if (type != 203) {
            return null;
        }

        return dis.readUTF();

    } catch (Exception e) {

        return null;
    }
}

    public static class ChunkPayload {

        public int chunkIndex;

        public byte[] data;

        public ChunkPayload(
                int chunkIndex,
                byte[] data
        ) {

            this.chunkIndex =
                    chunkIndex;

            this.data =
                    data;
        }
    }
}