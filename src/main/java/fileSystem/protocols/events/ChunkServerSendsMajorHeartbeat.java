package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

public class ChunkServerSendsMajorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

    // TODO: create file infrastructure to analyze, for the time being this
    //  - will just be a shell event to be filled in later

    public ChunkServerSendsMajorHeartbeat() {
        //TODO
    }


    @Override
    public int getType() {
        return type;
    }

}
