package filesystem.protocol.events;

import filesystem.protocol.Event;
import filesystem.protocol.Record;

import java.util.List;

import static filesystem.protocol.Protocol.CHUNK_SERVER_SENDS_MINOR_HEARTBEAT;

/**
 * Sent to the controller to confirm it is active
 * The minor heartbeat is simply the differences that have occurred since the last heartbeat
 */
public class ChunkHolderSendsMinorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MINOR_HEARTBEAT;
    private final List<Record> recentRecords;

    public ChunkHolderSendsMinorHeartbeat(List<Record> recentRecords) {
        this.recentRecords = recentRecords;
    }


    @Override
    public int getType() {
        return type;
    }

    public List<Record> getRecentRecords() {
        return recentRecords;
    }

    @Override
    public String toString() {
        return "ChunkHolderSendsMinorHeartbeat{" +
                       "recentRecords=" + recentRecords +
                       '}';
    }
}
