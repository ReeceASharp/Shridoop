package filesystem.protocol.events;

import filesystem.interfaces.Event;

import static filesystem.protocol.Protocol.CLIENT_REQUESTS_FILE;

public class ClientRequestsFile implements Event {
    private static final int type = CLIENT_REQUESTS_FILE;

    private final String filePath;


    public ClientRequestsFile(String file) {
        this.filePath = file;
    }


    @Override
    public int getType() {
        return type;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "ClientRequestsFile{" +
                       "file='" + filePath + '\'' +
                       '}';
    }
}
