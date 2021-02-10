package fileSystem.protocol;

import java.io.Serializable;

public interface Record extends Serializable {
    int FILE_ADD = 1;
    int FILE_DELETE = 2;

    int getType();
}
