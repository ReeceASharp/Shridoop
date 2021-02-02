package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkServerRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String originatingIP;
    private final int originatingPort;
    private final String name;

    public ChunkServerRequestsRegistration(String ip, int port, String name) {
        originatingIP = ip;
        originatingPort = port;
        this.name = name;

    }

    @Override
    public int getType() {
        return type;
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
