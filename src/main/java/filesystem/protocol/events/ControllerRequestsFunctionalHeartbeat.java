package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

/**
 * Used by the Controller to check for ChunkHolder failures, respond with a Protocol status code
 */
public class ControllerRequestsFunctionalHeartbeat implements Event {
    private static final int type = CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

    public ControllerRequestsFunctionalHeartbeat() { }


    @Override
    public int getType() {
        return type;
    }

}
