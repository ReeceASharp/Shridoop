package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CHUNK_SERVER_REPORTS_HEALTH_HEARTBEAT;

public class ChunkServerReportsHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_REPORTS_HEALTH_HEARTBEAT;

    private final int status;

    public ChunkServerReportsHeartbeat(int status) {
        this.status = status;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ChunkServerReportsFunctionalHeartbeat{" +
                       "status=" + status +
                       '}';
    }
}
