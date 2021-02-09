package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;

public class ControllerReportsFileRemoveStatus implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_FILE_REMOVE_STATUS;

    public ControllerReportsFileRemoveStatus() {
    }

    @Override
    public int getType() {
        return type;
    }
}
