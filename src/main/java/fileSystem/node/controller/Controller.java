package fileSystem.node.controller;

import fileSystem.node.Node;
import fileSystem.util.ConsoleParser;

public class Controller extends Node {

    public Controller() {

    }

    public static void main(String[] args) {

        Controller controller = new Controller();

        //create the console
        Thread console = new Thread(new ConsoleParser(controller));
        console.start();

    }


    @Override
    protected String getHelp() {
        return "Controller Help";
    }

    @Override
    protected String getIntro() {
        return "Controller Intro";
    }

    @Override
    protected String getCommands() {
        return "Controller Commands";
    }

    @Override
    public void onCommand(int type) {

    }
}
