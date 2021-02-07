package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_REQUESTS_FILE_ADD;

public class ClientRequestsFileAdd implements Event {
    private static final int type = CLIENT_REQUESTS_FILE_ADD;

    private final String file;
    private final int numberOfChunks;

    public ClientRequestsFileAdd(String file, int numberOfChunks) {
        this.file = file;
        this.numberOfChunks = numberOfChunks;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

    @Override
    public int getType() {
        return type;
    }

    public String getFile() {
        return file;
    }
}
