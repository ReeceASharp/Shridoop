package fileSystem.protocols;

public interface Event {
    /**
     * Returns a type to be used by the EventFactory to reconstruct the message on the receiving end
     * @return  a Protocol integer constant to differentiate between events
     */
    int getType();

    /**
     * Constructs a contiguous set of marshalledBytes representing the event's data to then be sent
     * through a socket to be reconstructed on the other side (essentially serialized)
     * @return  an event in byte form
     */
    byte[] getBytes();
}
