package filesystem.interfaces;

import java.io.Serializable;

public interface Record extends Serializable {
    int CHUNK_ADD = 1;
    int CHUNK_UPDATE = 2;
//    int CHUNK_DELETE = 3;

    int getType();
}
