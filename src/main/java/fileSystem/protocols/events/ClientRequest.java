package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CLIENT_REQUEST;

public class ClientRequest implements Event {
    private static final int type = CLIENT_REQUEST;

    private final int requestType;
    private final String file;


    public ClientRequest(int requestType, String file) {
        this.requestType = requestType;
        this.file = file;
    }


    @Override
    public int getType() {
        return type;
    }

    public int getRequestType() {
        return requestType;
    }

    public String getFile() {
        return file;
    }
}
