package fileSystem.node.controller;

import java.net.Socket;

public class ChunkData {
    public final String name;
    public final String address;
    public final int port;
    public final Socket socket;

    public ChunkData(String name, String address, int port, Socket socket) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Address: %s, Port: %s, %s", name, address, port, socket);
    }

    //TODO: Keep track of the current chunks each server contains


    @Override
    public boolean equals(Object obj) {
        try {
            ChunkData other = (ChunkData) obj;

            return other.name.equals(this.name) &&
                    other.address.equals(this.address) &&
                    other.port == port &&
                    other.socket.equals(socket);

        } catch (Exception e) {
            return false;
        }
    }
}
