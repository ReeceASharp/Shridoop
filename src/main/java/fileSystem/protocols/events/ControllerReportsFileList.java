package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import java.util.ArrayList;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_FILE_LIST;

public class ControllerReportsFileList implements Event {
    private static final int type = CONTROLLER_REPORTS_FILE_LIST;

    private final int status;
    private final ArrayList<String> files;


    public ControllerReportsFileList(int status, ArrayList<String> files) {
        this.status = status;
        this.files = files;
    }

    @Override
    public int getType() {
        return type;
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public int getStatus() {
        return status;
    }
}
