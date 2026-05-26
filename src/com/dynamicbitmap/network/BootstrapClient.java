package com.dynamicbitmap.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

public class BootstrapClient {

    private static final String BOOTSTRAP_URL =
            "http://localhost:8080";

    public static void register(
            String nodeId,
            int port
    ) {

        try {

            URL url =
                    new URL(
                            BOOTSTRAP_URL + "/register"
                    );

            HttpURLConnection conn =
                    (HttpURLConnection)
                            url.openConnection();

            conn.setRequestMethod("POST");

            conn.setDoOutput(true);

            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            String json =
                    "{"
                            + "\"nodeId\":\"" + nodeId + "\","
                            + "\"port\":" + port
                            + "}";

            OutputStream os =
                    conn.getOutputStream();

            os.write(
                    json.getBytes()
            );

            os.flush();

            os.close();

            conn.getResponseCode();

            conn.disconnect();

        } catch (Exception e) {

            System.out.println(
                    "No se pudo registrar en bootstrap"
            );
        }
    }

    public static List<NodeInfo> getPeers(
            String myNodeId
    ) {

        List<NodeInfo> peers =
                new ArrayList<>();

        try {

            URL url =
                    new URL(
                            BOOTSTRAP_URL
                                    + "/peers?nodeId="
                                    + myNodeId
                    );

            HttpURLConnection conn =
                    (HttpURLConnection)
                            url.openConnection();

            conn.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()
                            )
                    );

            String line;

            while (
                    (line = reader.readLine()) != null
            ) {

                line =
                        line.trim();

                if (
                        line.isEmpty()
                                ||
                                !line.contains("|")
                ) {
                    continue;
                }

                String[] parts =
                        line.split("\\|");

                if (parts.length != 3) {
                    continue;
                }

                String nodeId =
                        parts[0];

                String host =
                        parts[1];

                int port =
                        Integer.parseInt(
                                parts[2]
                        );

                peers.add(
                        new NodeInfo(
                                nodeId,
                                host,
                                port
                        )
                );
            }

            reader.close();

            conn.disconnect();

        } catch (Exception e) {

            System.out.println(
                    "No se pudieron obtener peers del bootstrap"
            );
        }

        return peers;
    }
}