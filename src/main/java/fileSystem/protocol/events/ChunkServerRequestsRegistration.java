package fileSystem.protocol.events;

import fileSystem.protocol.Event;

import static fileSystem.protocol.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkServerRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String serverName;
    private final String originatingHost;
    private final int originatingPort;



    public ChunkServerRequestsRegistration(String serverName, String host, int port) {
        this.serverName = serverName;
        this.originatingHost = host;
        this.originatingPort = port;
    }

    @Override
    public int getType() {
        return type;
    }

    public String getHost() {
        return originatingHost;
    }

    public int getPort() {
        return originatingPort;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public String toString() {
        return "ChunkServerRequestsRegistration{" +
                       "serverName='" + serverName + '\'' +
                       ", originatingHost='" + originatingHost + '\'' +
                       ", originatingPort=" + originatingPort +
                       '}';
    }
}
