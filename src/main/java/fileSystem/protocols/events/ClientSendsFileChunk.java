package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import java.util.ArrayList;

import static fileSystem.protocols.Protocol.CLIENT_SENDS_FILE_CHUNK;

public class ClientSendsFileChunk implements Event {
    private static final int type = CLIENT_SENDS_FILE_CHUNK;

    private final int chunkNumber;
    private final byte[] chunkData;
    private final ArrayList<String> serversToContact;

    public ClientSendsFileChunk(int chunkNumber, byte[] chunkData, ArrayList<String> serversToContact) {
        this.chunkNumber = chunkNumber;
        this.chunkData = chunkData;
        this.serversToContact = serversToContact;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public ArrayList<String> getServersToContact() {
        return serversToContact;
    }

    @Override
    public int getType() {
        return type;
    }
}
