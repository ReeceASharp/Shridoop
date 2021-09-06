package fileSystem.protocol.events;

import fileSystem.protocol.*;
import fileSystem.util.*;

import java.util.*;

public class ControllerReportsChunkGetList implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CHUNK_GET_LIST;

    private final int status;
    private final ArrayList<ContactList> chunkLocations;


    public ControllerReportsChunkGetList(int status, ArrayList<ContactList> chunkLocations) {
        this.status = status;
        this.chunkLocations = chunkLocations;
    }


    public int getStatus() {
        return status;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ControllerReportsChunkGetList{" +
                       "status=" + status +
                       ", chunkLocations=" + chunkLocations +
                       '}';
    }

    public ArrayList<ContactList> getChunkLocations() {
        return chunkLocations;
    }
}
