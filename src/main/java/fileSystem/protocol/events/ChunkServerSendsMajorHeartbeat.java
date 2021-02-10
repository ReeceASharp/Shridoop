package fileSystem.protocol.events;

import fileSystem.protocol.Event;

import java.util.ArrayList;

import static fileSystem.protocol.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

/**
 * The major heartbeat contains all current metadata about chunks stored in the Chunk Server
 *
 */
public class ChunkServerSendsMajorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

    private final ArrayList<String> currentChunks;

    public ChunkServerSendsMajorHeartbeat(ArrayList<String> currentChunks) {
        this.currentChunks = currentChunks;
    }


    @Override
    public int getType() {
        return type;
    }

}
