package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;
import fileSystem.util.ContactList;

import java.util.ArrayList;

/**
 * An event that is used in the case of either ADD, or GET, as the client has to contact these nodes directly
 * in order to not bottleneck through the Controller. This status will contain a RESPONSE_SUCCESS if a file
 * doesn't already exist in the filesystem at that path, and if the cluster is running
 */
public class ControllerReportsChunkAddList implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CHUNK_ADD_LIST;

    private final int status;
    private final ArrayList<ContactList> chunkDestinations;
    private final String file;

    //TODO: instead pass the location on the file system the file should be, not the actual file path
    public ControllerReportsChunkAddList(int status, String file, ArrayList<ContactList> chunkDestinations) {
        this.status = status;
        this.chunkDestinations = chunkDestinations;
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public ArrayList<ContactList> getChunkDestinations() {
        return chunkDestinations;
    }


}
