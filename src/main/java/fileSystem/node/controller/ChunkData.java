package fileSystem.node.controller;

public class ChunkData {
    public final String name;
    public final String address;
    public final int port;

    public ChunkData(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Address: %s, Port: %s", name, address, port);
    }

    //TODO: Keep track of the current chunks each server contains
}
