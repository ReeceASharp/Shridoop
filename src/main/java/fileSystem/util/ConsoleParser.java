package fileSystem.util;

import fileSystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.commons.text.
//import org.apache.commons.text.WordUtils;


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
        this.setup(node);

        this.node = node;
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

            String result;
            //Compare the input command with known commands
            try {
                Command func = commandMap.get(input.split(" ")[0].toLowerCase());
                result = func.runAction(input);
            } catch (NullPointerException npe) {
                result = "Invalid Command.";
            } catch (Exception e) {
                result = "Incorrect Parameters.";
            }

            if (result == null)
                return;

            System.out.println(result);
        }
    }

    private void setup(Node node) {
        resolveCommands(node);
    }

    private void resolveCommands(Node node) {

        this.commandMap.put("commands", userInput -> this.commandMap.keySet().toString());
        this.commandMap.put("help", userInput -> node.help());
        this.commandMap.put("quit", userInput -> null);

        // Get the node specific commands, and their mappings
        this.commandMap.putAll(node.getCommandMap());
    }

}
