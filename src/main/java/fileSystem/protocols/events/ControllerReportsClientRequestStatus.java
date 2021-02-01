package fileSystem.protocols.events;

import fileSystem.node.controller.ChunkData;
import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;

import java.util.ArrayList;

/**
 * Note: this class doesn't rely on another object using the serializable interface as it doesn't have set
 */
public class ControllerReportsClientRequestStatus implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CLIENT_REQUEST_STATUS;

    private final int requestType;
    private final int status;
    //private final ArrayList<ChunkData> serversToContact;

    public ControllerReportsClientRequestStatus(int requestType, int status, ArrayList<ChunkData> serversToContact) {
        this.requestType = requestType;
        this.status = status;
        //this.serversToContact = serversToContact;
    }

    @Override
    public int getType() {
        return type;
    }
}
