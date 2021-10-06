package filesystem.protocol.records;

import filesystem.protocol.Record;

public class ChunkUpdate implements Record {
    public static final int type = CHUNK_UPDATE;

    private final String filePath;
    private final int chunkNumber;
    private final String hash;
    private final int version;
//    private final String hash;

    public ChunkUpdate(String filePath, int chunkNumber, String hash, int version) {
        this.filePath = filePath;
        this.chunkNumber = chunkNumber;
        this.hash = hash;
        this.version = version;
//        this.hash = hash;
    }

    @Override
    public int getType() {
        return type;
    }
}
