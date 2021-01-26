package fileSystem.protocols;

import fileSystem.protocols.events.ChunkServerSendsRegistration;

import java.io.IOException;
import java.nio.ByteBuffer;

import static fileSystem.protocols.Protocol.*;

public class EventFactory {

    private static final EventFactory instance = new EventFactory();

    private EventFactory() {
    }

    public static EventFactory getInstance() {
        return instance;
    }

    public Event createEvent(byte[] marshalledBytes) throws IOException {
        //wrap in a buffer to grab the first integer stored, and the rest is a conversion to
        int eventType = ByteBuffer.wrap(marshalledBytes).getInt();

        switch (eventType) {
            // Controller -> ChunkServer
            case CONTROLLER_REPORTS_REGISTRATION_STATUS:
                break;
            case CONTROLLER_REQUESTS_MAJOR_HEARTBEAT:
                break;
            case CONTROLLER_REQUESTS_MINOR_HEARTBEAT:
                break;
            case CONTROLLER_REQUESTS_FILE_METADATA:
                break;
            // ChunkServer -> Controller
            case CHUNK_SERVER_SENDS_REGISTRATION:
                return new ChunkServerSendsRegistration(marshalledBytes);
            case CHUNK_SERVER_REPORTS_MAJOR_HEARTBEAT:
                break;
            case CHUNK_SERVER_REPORTS_MINOR_HEARTBEAT:
                break;
            case CHUNK_SERVER_REPORTS_FILE_CHUNK_METADATA:
                break;
            // ChunkServer -> ChunkServer
            case CHUNK_SERVER_REQUESTS_REPLICATION:
                break;
            case CHUNK_SERVER_REPORTS_REPLICATION:
                break;
            // Controller -> Client
            case CONTROLLER_REPORTS_FILE_CHUNK_ADD_DESTINATION:
                break;
            case CONTROLLER_REPORTS_FILE_CHUNK_REQUEST_LOCATION:
                break;
            case CONTROLLER_REPORTS_FILE_DELETE_STATUS:
                break;
            case CONTROLLER_REPORTS_CHUNK_SERVER_METADATA:
                break;
            case CONTROLLER_REPORTS_FILE_METADATA:
                break;
            // Client -> Controller
            case CLIENT_REQUESTS_FILE_SAVE:
                break;
            case CLIENT_REQUESTS_FILE:
                break;
            case CLIENT_REQUESTS_FILE_DELETE:
                break;
            case CLIENT_REQUESTS_CHUNK_SERVER_METADATA:
                break;
            case CLIENT_REQUESTS_FILE_METADATA:
                break;
            // Client -> ChunkServer
            case CLIENT_REQUESTS_FILE_CHUNK:
                break;
            // ChunkServer -> Client
            case CHUNK_SERVER_SENDS_FILE_CHUNK:
                break;
        }
        return null;
    }
}
