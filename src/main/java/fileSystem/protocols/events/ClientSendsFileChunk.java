package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_SENDS_FILE_CHUNK;

public class ClientSendsFileChunk implements Event {
    private static final int type = CLIENT_SENDS_FILE_CHUNK;
    private final byte[] chunkData;


    public ClientSendsFileChunk(byte[] data) {
        this.chunkData = data;
    }

    @Override
    public int getType() {
        return type;
    }
}
