package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CLIENT_REQUESTS_FILE_DELETE;

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

    @Override
    public String toString() {
        return "ClientRequestsFileDelete{" +
                       "file='" + file + '\'' +
                       '}';
    }
}
