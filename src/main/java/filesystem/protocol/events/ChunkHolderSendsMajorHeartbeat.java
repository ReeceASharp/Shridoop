package filesystem.protocol.events;

import filesystem.node.metadata.ChunkMetadata;
import filesystem.protocol.Event;

import java.util.ArrayList;

import static filesystem.protocol.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

/**
 * The major heartbeat contains all current metadata about chunks stored in the Chunk Server
 *
 */
public class ChunkHolderSendsMajorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

    private final ArrayList<ChunkMetadata> currentChunks;

    public ChunkHolderSendsMajorHeartbeat(ArrayList<ChunkMetadata> currentChunks) {
        this.currentChunks = currentChunks;
    }


    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ChunkHolderSendsMajorHeartbeat{" +
                       "currentChunks=" + currentChunks +
                       '}';
    }
}
