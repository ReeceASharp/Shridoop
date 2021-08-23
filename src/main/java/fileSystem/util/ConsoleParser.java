package fileSystem.util;

import fileSystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.commons.text.
//import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

/*
Used by an implementation of a Node to give functionality for entering commands
 */
public class ConsoleParser implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConsoleParser.class);

    private String[] commands = {"commands", "help", "quit"};


    private final Node node;
    private final Scanner userInput;

    public ConsoleParser(Node node) {
        this.node = node;
        // Concatenate the known commands
        this.setup();

        this.userInput = new Scanner(System.in);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        System.out.println(node.intro());

        parseInput();

        node.cleanup();
        logger.debug("Exiting ConsoleParser.");
    }

    private void parseInput() {

        while(true) {

            System.out.print("Command: ");
            String input = userInput.nextLine();


            // TODO: Rewrite logic to dump input into function
            if (Arrays.stream(this.commands).anyMatch(input.toLowerCase()::equals)) {

            }


            String response = null;
            switch (input.toLowerCase()) {
                case "commands":
                    response = Arrays.toString(this.commands);
                    break;
                case "help":
                    response = node.help();
                    break;
                case "quit":
                    response = null;
                    break;
                default:
                    try {
                        if (!node.handleCommand(input))
                            response = "ERROR: Invalid input. Enter 'help' for available commands.";
                    } catch (NullPointerException ne) {
                        //Can be thrown inside Client 'add' command
                        //executes on File not found when requesting it be added to the
                        System.out.print("File not found.");
                    }
            }

            if (response != null)
                System.out.println(response);
            else
                return;
        }
    }

    private void setup() {
        resolveCommands();
    }

    private void resolveCommands() {
        this.commands = Stream.concat(Arrays.stream(commands), Arrays.stream(this.node.commands())).toArray(String[]::new);
    }

    //private String resolveCommands() {
    //    String[][] commands;
    //
    //    // Console Commands for Controller Node
    //    //commands[0] = node.getConsoleText(CONSOLE_COMMANDS);
    //
    //
    //}

    //private String arraysToString(String[] arrayOne, ) {
    //
    //}
}
