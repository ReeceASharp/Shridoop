package fileSystem.util;

import fileSystem.node.Node;

import java.util.Scanner;

import static fileSystem.util.ConsoleConstant.*;

/*
Constructed by the relevant node, and allows commands to be entered there
Valid nodes: Client, Controller, ChunkServer
 */
public class ConsoleParser implements Runnable {

    private final Node node;
    private final Scanner userInput;

    public ConsoleParser(Node node) {
        this.node = node;

        this.userInput = new Scanner(System.in);
    }

    @Override
    public void run() {

        String info = node.getConsoleText(CONSOLE_INTRO);
        System.out.println(info);

        boolean quit = false;
        while (!quit) {
            quit = parseInput();
        }

        node.cleanup();

    }

    private boolean parseInput() {
        System.out.print("Command: ");
        String input = userInput.nextLine();

        String response = "";
        boolean quit = false;

        //Standard console commands, doesn't take into account custom, that is tried later and implemented in each node
        boolean tryCustom = false;
        switch (input.toLowerCase()) {
            case "commands":
                response = node.getConsoleText(CONSOLE_COMMANDS);
                break;
            case "help":
                response = node.getConsoleText(CONSOLE_HELP);
                break;
            case "quit":
            case "exit":
                quit = true;
                break;
            default:
                tryCustom = true;
        }

        //Node specific console commands
        //if (Arrays.asList(node.getCommands()).contains(input))
        if (tryCustom) {
            if (!node.handleCommand(input))
                response = "ERROR: Invalid input. Enter 'help' for available commands.";
        }

        if (!quit)
            System.out.println(response);

        return quit;

    }
}
