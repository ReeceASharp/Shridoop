package fileSystem.util;

import java.io.File;
import java.net.InetAddress;

/**
 * The structure of a Chunk, used by the
 */
public class Chunk {
    public final int chunkNumber;
    public final File name;
    //public final byte[] data;

    public Chunk(int chunkNumber, File name) {
        this.chunkNumber = chunkNumber;
        this.name = name;
        //this.data = data;
    }
}
