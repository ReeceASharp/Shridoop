package fileSystem.protocol.events;

import fileSystem.protocol.Event;
import fileSystem.protocol.Protocol;

public class ControllerReportsFileDeleteStatus implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_FILE_REMOVE_STATUS;
    private final int status;

    public ControllerReportsFileDeleteStatus(int status) {
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
