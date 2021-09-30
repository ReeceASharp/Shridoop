package filesystem.protocol.events;

import filesystem.protocol.Event;

import java.net.URL;

import static filesystem.protocol.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkServerRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String serverName;
    private final URL originatingURL;



    public ChunkServerRequestsRegistration(String serverName, URL url) {
        this.serverName = serverName;
        this.originatingURL = url;
    }

    @Override
    public int getType() {
        return type;
    }

    public URL getURL() {
        return originatingURL;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public String toString() {
        return "ChunkServerRequestsRegistration{" +
                       "serverName='" + serverName +
                       ", originatingURL='" + originatingURL +
                       '}';
    }
}
