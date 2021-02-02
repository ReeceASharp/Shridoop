package fileSystem.protocols.events;

import fileSystem.node.controller.ServerData;
import fileSystem.protocols.Event;
import fileSystem.protocols.Protocol;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;

/**
 * Note: this class doesn't rely on another object using the serializable interface as it doesn't have set
 */
public class ControllerReportsClientRequestStatus implements Event {
    private static final int type = Protocol.CONTROLLER_REPORTS_CLIENT_REQUEST_STATUS;

    private final int requestType;
    private final int status;
    private final ArrayList<ServerData> serversToContact;

    public ControllerReportsClientRequestStatus(int requestType, int status, ArrayList<ServerData> serversToContact) {
        this.requestType = requestType;
        this.status = status;
        this.serversToContact = serversToContact;
    }

    @Override
    public int getType() {
        return type;
    }
}
