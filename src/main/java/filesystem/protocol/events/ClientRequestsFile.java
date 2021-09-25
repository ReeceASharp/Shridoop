package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CLIENT_REQUESTS_FILE;

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

    @Override
    public String toString() {
        return "ClientRequestsFile{" +
                       "file='" + file + '\'' +
                       '}';
    }
}