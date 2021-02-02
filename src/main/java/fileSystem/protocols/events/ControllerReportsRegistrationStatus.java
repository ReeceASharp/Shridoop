package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_REGISTRATION_STATUS;

/**
 * Sent from the Controller to the ChunkServer acknowledging the server is setup and ready to go
 */
public class ControllerReportsRegistrationStatus implements Event {
    static final int type = CONTROLLER_REPORTS_REGISTRATION_STATUS;

    final int status;

    public ControllerReportsRegistrationStatus(int status) {
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
