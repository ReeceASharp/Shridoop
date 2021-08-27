package fileSystem.protocol.events;

import fileSystem.protocol.Event;
import fileSystem.protocol.Protocol;

import java.util.*;

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

    @Override
    public String toString() {
        return "ChunkServerSendsFileChunk{" +
                       "fileDataLength=" + fileData.length +
                       '}';
    }
}
