package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_REQUESTS_FILE_ADD;
import static fileSystem.protocols.Protocol.CLIENT_REQUESTS_FILE_DELETE;

public class ClientRequestsFileDelete implements Event {
    private static final int type = CLIENT_REQUESTS_FILE_DELETE;

    private final String file;


    public ClientRequestsFileDelete(String file) {
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
