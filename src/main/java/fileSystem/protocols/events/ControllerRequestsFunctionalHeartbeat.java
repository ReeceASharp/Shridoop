package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static fileSystem.protocols.Protocol.CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

/**
 * Used by the Controller to check for ChunkServer failures, respond with a Protocol status code
 */
public class ControllerRequestsFunctionalHeartbeat implements Event {
    private static final int type = CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

    public ControllerRequestsFunctionalHeartbeat() { }


    @Override
    public int getType() {
        return type;
    }

}
