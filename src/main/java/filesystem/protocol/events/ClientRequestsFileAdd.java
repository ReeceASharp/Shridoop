package filesystem.protocol.events;

import filesystem.protocol.Event;

import static filesystem.protocol.Protocol.CLIENT_REQUESTS_FILE_ADD;

public class ClientRequestsFileAdd implements Event {
    private static final int type = CLIENT_REQUESTS_FILE_ADD;

    private final String file;
    private final int numberOfChunks;
    private final long fileSize;

    @Override
    public String toString() {
        return "ClientRequestsFileAdd{" +
                       "file='" + file + '\'' +
                       ", numberOfChunks=" + numberOfChunks +
                       ", fileSize=" + fileSize +
                       '}';
    }

    public ClientRequestsFileAdd(String file, int numberOfChunks, long fileSize) {
        this.file = file;
        this.numberOfChunks = numberOfChunks;
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
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
