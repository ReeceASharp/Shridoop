package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT;

public class ChunkServerReportsFunctionalHeartbeat implements Event {
    private static final int type = CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT;

    private final int status;

    public ChunkServerReportsFunctionalHeartbeat(int status) {
        this.status = status;
    }

    public ChunkServerReportsFunctionalHeartbeat(byte[] marshalledBytes) throws IOException {
        InputWrapper wrapper = new InputWrapper(marshalledBytes);
        DataInputStream dataIn = wrapper.getDataIn();

        this.status = dataIn.readInt();

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

            dataOut.writeInt(status);

            data = wrapper.flushAndGetBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
