package fileSystem.protocols.events;

import fileSystem.node.controller.ServerData;
import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;

import java.util.ArrayList;

/**
 * An event that is used in the case of either ADD, or GET, as the client has to contact these nodes directly
 * in order to not bottleneck through the Controller
 */
public class ControllerReportsChunkAddList implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CHUNK_ADD_LIST;

    private final int status;
    private final ArrayList<ServerData> serversToContact;

    public ControllerReportsChunkAddList(int status, ArrayList<ServerData> serversToContact) {
        this.status = status;
        this.serversToContact = serversToContact;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public ArrayList<ServerData> getServersToContact() {
        return serversToContact;
    }
}
