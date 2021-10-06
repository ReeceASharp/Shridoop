package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CONTROLLER_REPORTS_REGISTRATION_STATUS;

/**
 * Sent from the Controller to the ChunkHolder acknowledging the server is setup and ready to go
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

    @Override
    public String toString() {
        return "ControllerReportsRegistrationStatus{" +
                       "status=" + status +
                       '}';
    }
}
