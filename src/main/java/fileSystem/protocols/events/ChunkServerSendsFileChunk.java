package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;

public class ChunkServerSendsFileChunk implements Event {
    private static final int type = Protocol.CHUNK_SERVER_SENDS_FILE_CHUNK;
    final byte[] fileData;

    public ChunkServerSendsFileChunk(byte[] fileData) {
        this.fileData = fileData;
    }

    @Override
    public int getType() {
        return type;
    }
}
