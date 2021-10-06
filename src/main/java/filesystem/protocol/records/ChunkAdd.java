package filesystem.protocol.records;

import filesystem.protocol.Record;

public class ChunkAdd implements Record {
    public static final int type = CHUNK_ADD;

    private final String filePath;
    private final int chunkNumber;
    private final String hash;

    public ChunkAdd(String filePath, int chunkNumber, String hash) {
        this.filePath = filePath;
        this.chunkNumber = chunkNumber;
        this.hash = hash;
    }

    @Override
    public int getType() {
        return type;
    }
}
