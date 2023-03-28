package filesystem.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LogConfiguration {
    public static void setSystemLogLevel(String logLevel) {
        Level l = Level.getLevel(logLevel);

        // Get the root logger, which is the parent of all loggers
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        loggerConfig.setLevel(l);

        ctx.updateLoggers();  // This causes all Loggers to re-fetch information from their LoggerConfig.
    }
}
