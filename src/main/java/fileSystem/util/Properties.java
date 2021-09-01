package fileSystem.util;

import fileSystem.node.*;

import java.io.*;
import java.util.*;

public class Properties {
    private static java.util.Properties properties;
    static {
        try {
            InputStream s = Node.class.getResourceAsStream("/config.properties");
            properties = new java.util.Properties();
            properties.load(s);
        } catch (IOException e) {
            System.out.println("Failed to fetch config variables. This will cause issues.");
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

}
