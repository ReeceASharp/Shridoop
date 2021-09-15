package filesystem.protocol.events;

import filesystem.protocol.Event;

import java.util.ArrayList;

import static filesystem.protocol.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

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

    @Override
    public String toString() {
        return "ChunkServerSendsMajorHeartbeat{" +
                       "currentChunks=" + currentChunks +
                       '}';
    }
}
