package filesystem.protocol.events;

import filesystem.interfaces.Event;
import filesystem.protocol.Protocol;

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
