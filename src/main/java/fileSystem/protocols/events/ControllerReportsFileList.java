package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_FILE_LIST;

public class ControllerReportsFileList implements Event {

    private static final int type = CONTROLLER_REPORTS_FILE_LIST;


    public ControllerReportsFileList() {
    }

    @Override
    public int getType() {
        return 0;
    }
}
