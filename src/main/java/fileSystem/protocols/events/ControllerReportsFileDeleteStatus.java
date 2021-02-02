package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_FILE_DELETE_STATUS;

public class ControllerReportsFileDeleteStatus implements Event {
    private static final int type = CONTROLLER_REPORTS_FILE_DELETE_STATUS;

    private final int status;

    public int getStatus() {
        return status;
    }

    public ControllerReportsFileDeleteStatus(int status) {
        this.status = status;
    }

    @Override
    public int getType() {
        return type;
    }
}
