package fileSystem.protocols.events;

import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.Event;
import fileSystem.protocols.OutputWrapper;

import java.io.*;

import static fileSystem.protocols.Protocol.CONTROLLER_REQUESTS_DEREGISTRATION;

public class ControllerRequestsDeregistration implements Event {

    static final int type = CONTROLLER_REQUESTS_DEREGISTRATION;

    // TODO: pass a hash or password on register to Controller, which it will then pass
    //  - back in order to authenticate deregister

    public ControllerRequestsDeregistration() { }

    public ControllerRequestsDeregistration(byte[] marshalledBytes) throws IOException {
        InputWrapper wrapper = new InputWrapper(marshalledBytes);

        //TODO

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

            //HASH

            data = wrapper.flushAndGetBytes();
        } catch (IOException e) {
            //failed for some reason
            e.printStackTrace();
        }

        return data;
    }
}
