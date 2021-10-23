package filesystem.protocol;


/**
 * Holds all constants and IDs relating to objects traveling over sockets. These are used to direct functionality on
 * actions to be taken when the object arrives ar its destination.
 *
 * Note: This holds quite a few constants. While this is sort of an anti-pattern as it gets larger, every variable
 * here isn't coupled with only one class, thus necessitating a central location to import from. The integer values
 * are irrelevant, they just need to be able to be unique.
 */
public final class Protocol {

    //
    public static final int RESPONSE_SUCCESS = 10;

    public static final int RESPONSE_FAILURE = 20;
    public static final int RESPONSE_FAILURE_FILE_NOT_FOUND = 21;
    public static final int RESPONSE_FAILURE_DUPLICATE_PATH = 22;


    // Heartbeat
    public static final int HEARTBEAT_MINOR = 50;
    public static final int HEARTBEAT_MAJOR = 51;
    public static final int HEARTBEAT_IGNORE_TYPE = 52;

    // Controller -> ChunkHolder
    public static final int CONTROLLER_REPORTS_REGISTRATION_STATUS = 100;
    public static final int CONTROLLER_REQUESTS_DEREGISTRATION = 101;
    public static final int CONTROLLER_REPORTS_SHUTDOWN = 102;
    public static final int CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT = 103;
    public static final int CONTROLLER_REQUESTS_CHUNK_DELETE = 104;

    // ChunkHolder -> Controller
    public static final int CHUNK_SERVER_REQUESTS_REGISTRATION = 110;
    public static final int CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS = 111;
    public static final int CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT = 112;
    public static final int CHUNK_SERVER_SENDS_MINOR_HEARTBEAT = 113;
    public static final int CHUNK_SERVER_REPORTS_HEALTH_HEARTBEAT = 114;


    // ChunkHolder -> ChunkHolder
    public static final int CHUNK_SERVER_REQUESTS_REPLICATION = 120;
    public static final int CHUNK_SERVER_REPORTS_REPLICATION = 121;

    // Controller -> Client
    public static final int CONTROLLER_REPORTS_FILE_LIST = 130;
    public static final int CONTROLLER_REPORTS_CHUNK_ADD_LIST = 131;
    public static final int CONTROLLER_REPORTS_CHUNK_GET_LIST = 132;
    public static final int CONTROLLER_REPORTS_FILE_REMOVE_STATUS = 133;
    public static final int CONTROLLER_REPORTS_CHUNK_SERVER_METADATA = 134;


    // Client -> Controller
    public static final int CLIENT_REQUESTS_FILE_ADD = 140;
    public static final int CLIENT_REQUESTS_FILE_DELETE = 141;
    public static final int CLIENT_REQUESTS_FILE = 142;
    public static final int CLIENT_REQUESTS_FILE_LIST = 143;
    //public static final int CLIENT_REQUESTS_CHUNK_SERVER_METADATA = 144;


    // Client -> ChunkHolder
    public static final int CLIENT_REQUESTS_FILE_CHUNK = 150;
    public static final int NODE_SENDS_FILE_CHUNK = 151;

    // ChunkHolder -> Client
    public static final int CHUNK_SERVER_SENDS_FILE_CHUNK = 161;

    private Protocol() {}
}
