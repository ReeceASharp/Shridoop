package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static fileSystem.protocols.Protocol.CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

/**
 * Used by the Controller to check for ChunkServer failures, respond with a Protocol status code
 */
public class ControllerRequestsFunctionalHeartbeat implements Event {
    private static final int type = CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT;

    public ControllerRequestsFunctionalHeartbeat() { }

    public ControllerRequestsFunctionalHeartbeat(byte[] marshalledBytes) throws IOException {
        InputWrapper wrapper = new InputWrapper(marshalledBytes);
        DataInputStream dataIn = wrapper.getDataIn();

        wrapper.close();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() {
        byte[] data = null;

        try {
            OutputWrapper wrapper = new OutputWrapper(type);
            DataOutputStream dataOut = wrapper.getDataOut();

            // the wrapper takes care of the event type, so nothing else specific to this class
            // needs to be written

            data = wrapper.flushAndGetBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
