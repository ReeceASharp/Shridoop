package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkServerRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String nickname;
    private final String originatingHost;
    private final int originatingPort;



    public ChunkServerRequestsRegistration(String nickname, String host, int port) {
        this.nickname = nickname;
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

    public String getNickname() {
        return nickname;
    }

}
