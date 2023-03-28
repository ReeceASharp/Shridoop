package filesystem.protocol.records;

import filesystem.interfaces.Record;

public class ChunkUpdate implements Record {
    public static final int type = CHUNK_UPDATE;

    public final String filePath;
    public final int chunkNumber;
    public final String hash;
    public final int version;

    public ChunkUpdate(String filePath, int chunkNumber, String hash, int version) {
        this.filePath = filePath;
        this.chunkNumber = chunkNumber;
        this.hash = hash;
        this.version = version;
    }

    @Override
    public int getType() {
        return type;
    }

}
