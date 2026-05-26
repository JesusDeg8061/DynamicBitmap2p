package com.dynamicbitmap.network;

public class NodeInfo {

    public String nodeId;
    public String host;
    public int port;

    public NodeInfo(String host, int port) {

        this.nodeId = null;
        this.host = host;
        this.port = port;
    }

    public NodeInfo(String nodeId, String host, int port) {

        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
    }

    public boolean hasNodeId() {

        return nodeId != null && !nodeId.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NodeInfo)) {
            return false;
        }

        NodeInfo other =
                (NodeInfo) obj;

        if (this.hasNodeId() && other.hasNodeId()) {

            return this.nodeId.equals(other.nodeId);
        }

        return this.host.equals(other.host)
                &&
                this.port == other.port;
    }

    @Override
    public int hashCode() {

        if (hasNodeId()) {
            return nodeId.hashCode();
        }

        return (host + ":" + port).hashCode();
    }
}