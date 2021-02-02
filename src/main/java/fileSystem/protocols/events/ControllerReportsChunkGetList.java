package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;

public class ControllerReportsChunkGetList implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CHUNK_GET_LIST;

    //whether the request is valid
    private final int status;

    public ControllerReportsChunkGetList(int status) {
        this.status = status;

        //TODO create a data structure that holds the metadata of the chunks and their associated ChunkServers
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int getType() {
        return type;
    }
}
