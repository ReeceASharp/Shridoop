package fileSystem.protocol.records;

import fileSystem.protocol.Record;

public class FileAdd implements Record {
    public static final int type = FILE_ADD;

    public final String file;


    public FileAdd(String file) {
        this.file = file;
    }


    @Override
    public int getType() {
        return type;
    }
}
