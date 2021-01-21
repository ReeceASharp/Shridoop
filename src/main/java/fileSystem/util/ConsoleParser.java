package fileSystem.util;

import fileSystem.node.Node;

import static fileSystem.util.ConsoleConstant.*;

import java.util.Arrays;
import java.util.Scanner;

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

        //Standard console commands, doesn't take into account
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
                response = "ERROR: Invalid input. Enter 'help' for available commands.";
        }

        //Node specific console commands
        if (Arrays.asList(node.getCommands()).contains(input))
            node.handleCommand(input);

        if (!quit)
            System.out.println(response);

        return quit;

    }
}
