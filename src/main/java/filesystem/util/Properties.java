package filesystem.util;

import filesystem.node.Node;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Properties {
    private static final java.util.Properties properties;
    private static final Map<String, String> env;

    // Cache config variables on first attempt to fetch
    static {
        try {
            InputStream s = Node.class.getResourceAsStream("/config.properties");
            properties = new java.util.Properties();
            properties.load(s);

            env = System.getenv();

        } catch (IOException e) {
            System.out.println("Failed to fetch config variables. Exiting...");
            // Rethrow exception to exit program
            throw new RuntimeException(e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Get environment variable from the cached map
     * @param key key of the environment variable
     * @return value of the environment variable
     */
    public static String getEnv(String key) {
        return env.get(key);
    }

    /**
     * Get environment variable from the cached map and parse it to an int
     * @param key key of the environment variable
     * @return value of the environment variable
     */
    public static int getEnvInt(String key) {
        return Integer.parseInt(env.get(key));
    }
}
