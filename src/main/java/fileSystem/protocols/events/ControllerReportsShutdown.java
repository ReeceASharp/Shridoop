package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.IOException;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_SHUTDOWN;

/**
 * Used by the Controller to signal the ChunkServer to shutdown the socket. This is used
 * as without it, the ChunkServer sends off its ChunkServerReportsDeregistrationStatus, then
 * immediately cleans up and exits. This creates a race condition for the Controller to read
 * the socket before it closes
 */
public class ControllerReportsShutdown implements Event {

    static final int type = CONTROLLER_REPORTS_SHUTDOWN;

    public ControllerReportsShutdown() {
    }

    public ControllerReportsShutdown(byte[] marshalledBytes) throws IOException {

        InputWrapper wrapper = new InputWrapper(marshalledBytes);

        //this class is a bit empty, could possibly pass some sort of verification

        //close wrapper streams
        wrapper.close();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() {
        byte[] data = null;
        //create a wrapper around the bytes to leverage some methods to easily extract values


        try {
            OutputWrapper wrapper = new OutputWrapper(type);

            data = wrapper.flushAndGetBytes();
        } catch (IOException e) {
            //failed for some reason
            e.printStackTrace();
        }

        return data;
    }
}
