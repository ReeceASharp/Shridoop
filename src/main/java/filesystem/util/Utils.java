package filesystem.util;

import java.time.Instant;

public final class Utils {
    public static void appendLn(StringBuilder sb, String toAppend) {
        sb.append(toAppend).append(System.getProperty("line.separator"));
    }

    public static String timestampNowString() {
        return Instant.now().toString();
    }
}