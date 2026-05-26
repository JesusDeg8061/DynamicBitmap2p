package com.dynamicbitmap.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BootstrapServer {

    private static final int PORT = 8080;

    private static final Map<String, PeerInfo> peers =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try {

            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(PORT),
                            0
                    );

            server.createContext(
                    "/register",
                    BootstrapServer::handleRegister
            );

            server.createContext(
                    "/peers",
                    BootstrapServer::handlePeers
            );

            server.setExecutor(
                    java.util.concurrent.Executors.newCachedThreadPool()
            );

            server.start();

            System.out.println(
                    "BootstrapServer iniciado en puerto "
                            + PORT
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void handleRegister(
            HttpExchange exchange
    ) {

        try {

            if (
                    !"POST".equalsIgnoreCase(
                            exchange.getRequestMethod()
                    )
            ) {

                send(
                        exchange,
                        405,
                        "Method not allowed"
                );

                return;
            }

            String body =
                    new String(
                            exchange
                                    .getRequestBody()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            String nodeId =
                    extractString(
                            body,
                            "nodeId"
                    );

            int port =
                    extractInt(
                            body,
                            "port"
                    );

            String host =
                    exchange
                            .getRemoteAddress()
                            .getAddress()
                            .getHostAddress();

            if (
                    nodeId == null
                            ||
                            nodeId.isEmpty()
                            ||
                            port <= 0
            ) {

                send(
                        exchange,
                        400,
                        "Invalid data"
                );

                return;
            }

            peers.put(
                    nodeId,
                    new PeerInfo(
                            nodeId,
                            host,
                            port,
                            System.currentTimeMillis()
                    )
            );

            System.out.println(
                    "Registrado: "
                            + nodeId
                            + " -> "
                            + host
                            + ":"
                            + port
            );

            send(
                    exchange,
                    200,
                    "OK"
            );

        } catch (Exception e) {

            e.printStackTrace();

            try {

                send(
                        exchange,
                        500,
                        "ERROR"
                );

            } catch (Exception ignored) {
            }
        }
    }

    private static void handlePeers(
            HttpExchange exchange
    ) {

        try {

            if (
                    !"GET".equalsIgnoreCase(
                            exchange.getRequestMethod()
                    )
            ) {

                send(
                        exchange,
                        405,
                        "Method not allowed"
                );

                return;
            }

            String query =
                    exchange
                            .getRequestURI()
                            .getQuery();

            String myNodeId =
                    null;

            if (query != null) {

                for (String part : query.split("&")) {

                    String[] kv =
                            part.split("=");

                    if (
                            kv.length == 2
                                    &&
                                    kv[0].equals("nodeId")
                    ) {

                        myNodeId = kv[1];
                    }
                }
            }

            long now =
                    System.currentTimeMillis();

            StringBuilder response =
                    new StringBuilder();

            for (PeerInfo peer : peers.values()) {

                if (
                        now - peer.lastSeen
                                > 30000
                ) {

                    continue;
                }

                if (
                        myNodeId != null
                                &&
                                myNodeId.equals(
                                        peer.nodeId
                                )
                ) {

                    continue;
                }

                response.append(peer.nodeId)
                        .append("|")
                        .append(peer.host)
                        .append("|")
                        .append(peer.port)
                        .append("\n");
            }

            send(
                    exchange,
                    200,
                    response.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            try {

                send(
                        exchange,
                        500,
                        "ERROR"
                );

            } catch (Exception ignored) {
            }
        }
    }

    private static void send(
            HttpExchange exchange,
            int status,
            String response
    ) throws Exception {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        OutputStream os =
                exchange.getResponseBody();

        os.write(bytes);

        os.close();
    }

    private static String extractString(
            String json,
            String key
    ) {

        String pattern =
                "\"" + key + "\"";

        int keyIndex =
                json.indexOf(pattern);

        if (keyIndex == -1) {
            return null;
        }

        int colon =
                json.indexOf(
                        ":",
                        keyIndex
                );

        int firstQuote =
                json.indexOf(
                        "\"",
                        colon + 1
                );

        int secondQuote =
                json.indexOf(
                        "\"",
                        firstQuote + 1
                );

        if (
                colon == -1
                        ||
                        firstQuote == -1
                        ||
                        secondQuote == -1
        ) {

            return null;
        }

        return json.substring(
                firstQuote + 1,
                secondQuote
        );
    }

    private static int extractInt(
            String json,
            String key
    ) {

        try {

            String pattern =
                    "\"" + key + "\"";

            int keyIndex =
                    json.indexOf(pattern);

            if (keyIndex == -1) {
                return -1;
            }

            int colon =
                    json.indexOf(
                            ":",
                            keyIndex
                    );

            int end =
                    json.indexOf(
                            "}",
                            colon
                    );

            if (
                    colon == -1
                            ||
                            end == -1
            ) {

                return -1;
            }

            String value =
                    json.substring(
                            colon + 1,
                            end
                    ).trim();

            if (value.contains(",")) {

                value =
                        value.substring(
                                0,
                                value.indexOf(",")
                        ).trim();
            }

            return Integer.parseInt(value);

        } catch (Exception e) {

            return -1;
        }
    }

    private static class PeerInfo {

        String nodeId;
        String host;
        int port;
        long lastSeen;

        PeerInfo(
                String nodeId,
                String host,
                int port,
                long lastSeen
        ) {

            this.nodeId = nodeId;
            this.host = host;
            this.port = port;
            this.lastSeen = lastSeen;
        }
    }
}