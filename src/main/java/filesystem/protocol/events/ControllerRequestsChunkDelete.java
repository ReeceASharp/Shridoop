package filesystem.protocol.events;

import filesystem.protocol.*;

import static filesystem.protocol.Protocol.CONTROLLER_REQUESTS_CHUNK_DELETE;


public class ControllerRequestsChunkDelete implements Event {
    public static final int type = CONTROLLER_REQUESTS_CHUNK_DELETE;

    @Override
    public int getType() {
        return type;
    }
}
