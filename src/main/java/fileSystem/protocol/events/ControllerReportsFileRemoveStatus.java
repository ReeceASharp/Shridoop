package fileSystem.protocol.events;

import fileSystem.protocol.Event;
import fileSystem.protocol.Protocol;

public class ControllerReportsFileRemoveStatus implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_FILE_REMOVE_STATUS;

    public ControllerReportsFileRemoveStatus() {
    }

    @Override
    public int getType() {
        return type;
    }
}
