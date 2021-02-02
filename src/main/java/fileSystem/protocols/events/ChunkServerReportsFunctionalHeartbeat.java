package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT;

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
}
