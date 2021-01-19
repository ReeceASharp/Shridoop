package fileSystem.node.client;

import fileSystem.node.Node;
import fileSystem.util.ConsoleParser;

public class Client extends Node {

    public Client() {

    }

    public static void main(String[] args) {
        //TODO: parse inputs and setup TCP connection

        Client client = new Client();

        //Console parser
        Thread console = new Thread(new ConsoleParser(client));
        console.start();

    }

    @Override
    protected String getHelp() {
        return "Client Help";
    }

    @Override
    protected String getIntro() {
        return "Client Intro";
    }

    @Override
    protected String getCommands() {
        return "Client Commands";
    }

    @Override
    public void onCommand(int type) {

    }
}
