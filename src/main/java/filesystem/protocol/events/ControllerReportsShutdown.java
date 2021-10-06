package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CONTROLLER_REPORTS_SHUTDOWN;

/**
 * Used by the Controller to signal the ChunkHolder to shutdown the socket. This is used
 * as without it, the ChunkHolder sends off its ChunkHolderReportsDeregistrationStatus, then
 * immediately cleans up and exits. This creates a race condition for the Controller to read
 * the socket before it closes
 */
public class ControllerReportsShutdown implements Event {

    static final int type = CONTROLLER_REPORTS_SHUTDOWN;

    public ControllerReportsShutdown() {
    }



    @Override
    public int getType() {
        return type;
    }

}
