package fileSystem.util;

public final class Utils {
    public static void appendLn(StringBuilder sb, String toAppend) {
        sb.append(toAppend).append(System.getProperty("line.separator"));
    }
}