package fileSystem.util;

import fileSystem.node.Node;
import static fileSystem.util.ConsoleConstants.*;

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

        while(true) {
            parseInput();
        }

    }

    private void parseInput() {
        System.out.print("Command: ");
        String input = userInput.nextLine();

    }
}
