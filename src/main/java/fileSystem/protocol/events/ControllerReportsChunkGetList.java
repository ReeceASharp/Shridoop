package fileSystem.protocol.events;

import fileSystem.protocol.*;
import fileSystem.util.*;

import java.util.*;

public class ControllerReportsChunkGetList implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CHUNK_GET_LIST;

    private final int status;
    private final int numberOfChunks;
    private final ArrayList<ContactList> chunkLocations;


    public ControllerReportsChunkGetList(int status, int numberOfChunks, ArrayList<ContactList> chunkLocations) {
        this.status = status;
        this.numberOfChunks = numberOfChunks;
        this.chunkLocations = chunkLocations;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
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
                       ", numberOfChunks=" + numberOfChunks +
                       '}';
    }

    public ArrayList<ContactList> getChunkLocations() {
        return chunkLocations;
    }
}
