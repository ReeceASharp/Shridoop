package fileSystem.protocol.records;

import fileSystem.protocol.Record;

public class FileDelete implements Record {
    public static final int type = FILE_DELETE;

    public final String file;

    public FileDelete(String file) {
        this.file = file;
    }

    @Override
    public int getType() {
        return type;
    }
}
