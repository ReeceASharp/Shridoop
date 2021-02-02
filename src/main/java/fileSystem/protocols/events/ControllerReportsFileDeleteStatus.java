package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_FILE_DELETE_STATUS;

public class ControllerReportsFileDeleteStatus implements Event {
    private static final int type = CONTROLLER_REPORTS_FILE_DELETE_STATUS;

    @Override
    public int getType() {
        return type;
    }
}
