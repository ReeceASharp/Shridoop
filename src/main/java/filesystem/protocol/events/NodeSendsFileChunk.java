package filesystem.protocol.events;

import filesystem.protocol.Event;

import java.net.URL;
import java.util.ArrayList;

import static filesystem.protocol.Protocol.NODE_SENDS_FILE_CHUNK;

public class NodeSendsFileChunk implements Event {
    private static final int type = NODE_SENDS_FILE_CHUNK;

    private final String fileName;
    private final int chunkNumber;
    private final byte[] chunkData;
    private final String hash;
    private final ArrayList<URL> serversToContact;

    public NodeSendsFileChunk(String fileName,
                              int chunkNumber,
                              byte[] chunkData,
                              String hash,
                              ArrayList<URL> serversToContact) {
        this.fileName = fileName;
        this.chunkNumber = chunkNumber;
        this.chunkData = chunkData;
        this.hash = hash;
        this.serversToContact = serversToContact;
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public ArrayList<URL> getServersToContact() {
        return serversToContact;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "NodeSendsFileChunk{" +
                       "fileName='" + fileName + '\'' +
                       ", chunkNumber=" + chunkNumber +
                       ", chunkData=" + chunkData.length +
                       ", hash='" + hash + '\'' +
                       ", serversToContact=" + serversToContact +
                       '}';
    }
}
