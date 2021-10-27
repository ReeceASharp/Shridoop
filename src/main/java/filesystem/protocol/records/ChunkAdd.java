package filesystem.protocol.records;

import filesystem.protocol.Record;

public class ChunkAdd implements Record {
    public static final int type = CHUNK_ADD;

    public final String filePath;
    public final int chunkNumber;
    public final String hash;
    public final int chunkSize;

    public ChunkAdd(String filePath, int chunkNumber, String hash, int chunkSize) {
        this.filePath = filePath;
        this.chunkNumber = chunkNumber;
        this.hash = hash;
        this.chunkSize = chunkSize;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ChunkAdd{" +
                       "filePath='" + filePath + '\'' +
                       ", chunkNumber=" + chunkNumber +
                       ", hash='" + hash + '\'' +
                       '}';
    }
}
