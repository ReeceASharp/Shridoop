package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_REQUESTS_FILE;

public class ClientRequestsFile implements Event {
    private static final int type = CLIENT_REQUESTS_FILE;

    private final String file;


    public ClientRequestsFile(String file) {
        this.file = file;
    }


    @Override
    public int getType() {
        return type;
    }

    public String getFile() {
        return file;
    }
}
