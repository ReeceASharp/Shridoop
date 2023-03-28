package filesystem.protocol.events;

import filesystem.interfaces.Event;
import filesystem.util.HostPortAddress;



import static filesystem.protocol.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkHolderRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String serverName;
    private final HostPortAddress holderAddress;



    public ChunkHolderRequestsRegistration(String serverName, HostPortAddress address) {
        this.serverName = serverName;
        this.holderAddress = address;
    }

    @Override
    public int getType() {
        return type;
    }

    public HostPortAddress getHolderAddress() {
        return holderAddress;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public String toString() {
        return "ChunkHolderRequestsRegistration{" +
                       "serverName='" + serverName +
                       ", getHolderAddress='" + holderAddress +
                       '}';
    }
}
