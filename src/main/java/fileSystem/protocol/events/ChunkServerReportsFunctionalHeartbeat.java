package fileSystem.protocol.events;

import fileSystem.protocol.Event;

import static fileSystem.protocol.Protocol.CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT;

public class ChunkServerReportsFunctionalHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT;

    private final int status;

    public ChunkServerReportsFunctionalHeartbeat(int status) {
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
