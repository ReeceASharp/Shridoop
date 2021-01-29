package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.DataOutputStream;
import java.io.IOException;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

public class ChunkServerSendsMajorHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT;

    // TODO: create file infrastructure to analyze, for the time being this
    //  - will just be a shell event to be filled in later

    public ChunkServerSendsMajorHeartbeat() {
        //TODO
    }

    public ChunkServerSendsMajorHeartbeat(byte[] marshalledBytes) throws IOException {
        InputWrapper wrapper = new InputWrapper(marshalledBytes);

        //Data extract here

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

            //Data load here

            data = wrapper.flushAndGetBytes();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
