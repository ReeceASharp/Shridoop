package fileSystem.protocols;

import java.io.Serializable;

public interface Event extends Serializable {
    /**
     * Returns a type to be used by the EventFactory to reconstruct the message on the receiving end
     *
     * @return a Protocol integer constant to differentiate between events
     */
    int getType();
}
