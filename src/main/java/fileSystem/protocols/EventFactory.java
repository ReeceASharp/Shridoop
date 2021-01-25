package fileSystem.protocols;

import static fileSystem.protocols.Protocol.*;

import java.nio.ByteBuffer;

public class EventFactory {

    private static final EventFactory instance = new EventFactory();

    private EventFactory() { }

    public static EventFactory getInstance() {
        return instance;
    }

    public Event createEvent(byte[] marshalledBytes) {
        //wrap in a buffer to grab the first integer stored, and the rest is a conversion to
        int eventType = ByteBuffer.wrap(marshalledBytes).getInt();

        switch (eventType) {
            case CHUNK_SERVER_SENDS_REGISTRATION:
                break;
            case CONTROLLER_REPORTS_REGISTRATION_STATUS:
                break;
            case CLIENT_SENDS_FILE_SAVE_REQUEST:
                break;
            case CONTROLLER_SENDS_CHUNK_SERVER_LIST:
                break;


        }
        return null;
    }
}
