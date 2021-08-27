package fileSystem.protocol.events;

import fileSystem.protocol.Event;
import fileSystem.protocol.Protocol;
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
    private final String file;
    private final ArrayList<ContactList> chunkDestinations;

    public ControllerReportsChunkAddList(int status,
                                         String file,
                                         ArrayList<ContactList> chunkDestinations) {
        this.status = status;
        this.file = file;
        this.chunkDestinations = chunkDestinations;
    }

    @Override
    public String toString() {
        return "ControllerReportsChunkAddList{" +
                       "status=" + status +
                       ", file='" + file + '\'' +
                       ", chunkDestinations=" + chunkDestinations +
                       '}';
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
