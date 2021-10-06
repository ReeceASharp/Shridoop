package filesystem.protocol.events;

import filesystem.protocol.Event;

import java.net.InetSocketAddress;

import static filesystem.protocol.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkHolderRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String serverName;
    private final InetSocketAddress holderAddress;



    public ChunkHolderRequestsRegistration(String serverName, InetSocketAddress address) {
        this.serverName = serverName;
        this.holderAddress = address;
    }

    @Override
    public int getType() {
        return type;
    }

    public InetSocketAddress getHolderAddress() {
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
