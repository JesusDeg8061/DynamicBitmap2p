package com.dynamicbitmap.network;

public interface RelayMessageHandler {

    void onRelayMessage(
            String fromNodeId,
            byte[] payload
    );
}