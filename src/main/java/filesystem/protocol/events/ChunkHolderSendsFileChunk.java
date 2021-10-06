package filesystem.protocol.events;

import filesystem.protocol.Event;
import filesystem.protocol.Protocol;

public class ChunkHolderSendsFileChunk implements Event {
    private static final int type = Protocol.CHUNK_SERVER_SENDS_FILE_CHUNK;
    final byte[] fileData;

    public ChunkHolderSendsFileChunk(byte[] fileData) {
        this.fileData = fileData;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ChunkHolderSendsFileChunk{" +
                       "fileDataLength=" + fileData.length +
                       '}';
    }
}
