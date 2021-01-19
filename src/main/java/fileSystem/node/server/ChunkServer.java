package fileSystem.node.server;

import fileSystem.node.Node;
import fileSystem.util.ConsoleParser;

public class ChunkServer extends Node {

    public ChunkServer() {

    }

    public static void main(String[] args) {

        ChunkServer server = new ChunkServer();

        //create the console, this may be refactored to not exist, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();
    }


    @Override
    protected String getHelp() {
        return "ChunkServer Help";
    }

    @Override
    protected String getIntro() {
        return "ChunkServer Intro";
    }

    @Override
    protected String getCommands() {
        return "ChunkServer Commands";
    }

    @Override
    public void onCommand(int type) {

    }
}
