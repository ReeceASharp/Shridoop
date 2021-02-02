package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_REQUESTS_FILE_CHUNK;

public class ClientRequestsFileChunk implements Event {
    private static final int type = CLIENT_REQUESTS_FILE_CHUNK;

    private final String file;

    public ClientRequestsFileChunk(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Override
    public int getType() {
        return type;
    }
}
