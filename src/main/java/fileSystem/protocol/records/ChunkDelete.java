package fileSystem.protocol.records;

import fileSystem.protocol.Record;

public class ChunkDelete implements Record {
    public static final int type = FILE_DELETE;

    private final String filePath;
    private final int chunkNumber;

    public ChunkDelete(String filePath, int chunkNumber) {
        this.filePath = filePath;
        this.chunkNumber = chunkNumber;
    }

    @Override
    public int getType() {
        return type;
    }
}
