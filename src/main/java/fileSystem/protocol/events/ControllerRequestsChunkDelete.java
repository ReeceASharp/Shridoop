package fileSystem.protocol.events;

import fileSystem.protocol.*;

import static fileSystem.protocol.Protocol.CONTROLLER_REQUESTS_CHUNK_DELETE;


public class ControllerRequestsChunkDelete implements Event {
    public static final int type = CONTROLLER_REQUESTS_CHUNK_DELETE;

    @Override
    public int getType() {
        return type;
    }
}
