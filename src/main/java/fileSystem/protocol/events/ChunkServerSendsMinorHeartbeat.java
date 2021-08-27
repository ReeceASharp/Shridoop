package fileSystem.protocol.events;

import fileSystem.protocol.Event;

import static fileSystem.protocol.Protocol.CHUNK_SERVER_SENDS_MINOR_HEARTBEAT;

/**
 * Sent to the controller to confirm it is active
 * The minor heartbeat is simply the differences that have occurred since the last heartbeat
 */
public class ChunkServerSendsMinorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MINOR_HEARTBEAT;


    // TODO: create file infrastructure to analyze, for the time being this
    //  - will just be a shell event to be filled in later

    public ChunkServerSendsMinorHeartbeat() {
        //TODO
    }


    @Override
    public int getType() {
        return type;
    }
}
