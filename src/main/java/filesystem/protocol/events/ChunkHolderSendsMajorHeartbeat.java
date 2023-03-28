package filesystem.protocol.events;

import filesystem.node.metadata.ChunkMetadata;
import filesystem.interfaces.Event;

import java.util.ArrayList;

import static filesystem.protocol.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

/**
 * The major heartbeat contains all current metadata about chunks stored in the Chunk Server
 */
public class ChunkHolderSendsMajorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;
    private final ArrayList<ChunkMetadata> currentChunks;
    private final int totalChunks;

    public ChunkHolderSendsMajorHeartbeat(ArrayList<ChunkMetadata> currentChunks, int totalChunks) {
        this.currentChunks = currentChunks;
        this.totalChunks =totalChunks;
    }

    public ArrayList<ChunkMetadata> getCurrentChunks() {
        return currentChunks;
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
