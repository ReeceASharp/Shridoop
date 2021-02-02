package fileSystem.protocols.events;

import fileSystem.protocols.Event;

public class ClientRequestsFileChunk implements Event {
    private final String file;

    public ClientRequestsFileChunk(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Override
    public int getType() {
        return 0;
    }
}
