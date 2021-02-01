package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS;

/**
 * Sent from the ChunkServer to the Controller confirming its request for deregistration, and by
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
}
