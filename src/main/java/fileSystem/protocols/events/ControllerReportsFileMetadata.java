package fileSystem.protocols.events;

import fileSystem.protocols.Event;

public class ControllerReportsFileMetadata implements Event {



    public ControllerReportsFileMetadata() {
    }

    @Override
    public int getType() {
        return 0;
    }
}
