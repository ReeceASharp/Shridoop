package fileSystem.protocol.events;

import fileSystem.protocol.Event;

import static fileSystem.protocol.Protocol.CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS;

/**
 * Sent from the ChunkServer to the Controller confirming its request to deregister, and by
 * extension shutdown
 */
public class ChunkServerReportsDeregistrationStatus implements Event {
    static final int type = CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS;

    private final int status;
    private final String originatingIP;
    private final int originatingPort;
    private final String name;

    public ChunkServerReportsDeregistrationStatus(int status, String ip, int port, String name) {
        this.status = status;
        originatingIP = ip;
        originatingPort = port;
        this.name = name;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public String getIP() {
        return originatingIP;
    }

    public int getPort() {
        return originatingPort;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChunkServerReportsDeregistrationStatus{" +
                       "status=" + status +
                       ", originatingIP='" + originatingIP + '\'' +
                       ", originatingPort=" + originatingPort +
                       ", name='" + name + '\'' +
                       '}';
    }
}
