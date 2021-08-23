package fileSystem.util;

import fileSystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.commons.text.
//import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
Used by an implementation of a Node to give functionality for entering commands
 */
public class ConsoleParser implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConsoleParser.class);

    private final Map<String, Command> commandMap = new HashMap<>();

    private final Node node;
    private final Scanner userInput;


    public ConsoleParser(Node node) {
        this.node = node;
        this.userInput = new Scanner(System.in);

        this.setup();
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


            // TODO: Rewrite logic to use commandMap
            //if (Arrays.stream(this.commands).anyMatch(input.toLowerCase()::equals)) {
            //
            //}


            //String response = null;
            //switch (input.toLowerCase()) {
            //    case "commands":
            //        //response = Arrays.toString(this.commands);
            //        break;
            //    case "help":
            //        //response = node.help();
            //        break;
            //    case "quit":
            //        //response = null;
            //        break;
            //    default:
            //        try {
            //            if (!node.handleCommand(input))
            //                response = "ERROR: Invalid input. Enter 'help' for available commands.";
            //        } catch (NullPointerException ne) {
            //            //Can be thrown inside Client 'add' command
            //            //executes on File not found when requesting it be added to the
            //            System.out.print("File not found.");
            //        }
            //}
            //
            //if (response != null)
            //    System.out.println(response);
            //else
            //    return;
        }
    }

    private void setup() {
        resolveCommands();
    }

    private void resolveCommands() {

        this.commandMap.put("commands", userInput -> this.commandMap.keySet().toString());
        this.commandMap.put("help", userInput -> this.node.help());
        this.commandMap.put("quit", userInput -> null);

        // Get the node specific commands, and their mappings
        this.commandMap.putAll(this.node.getCommandMap());


        //this.commands = Stream.concat(Arrays.stream(commands), Arrays.stream(this.node.commands())).toArray(String[]::new);
    }

}
