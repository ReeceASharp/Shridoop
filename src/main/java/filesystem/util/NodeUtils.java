package filesystem.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import java.net.Socket;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public final class NodeUtils {
    public static void appendLn(StringBuilder sb, String toAppend) {
        sb.append(toAppend).append(System.getProperty("line.separator"));
    }

    public static HostPortAddress socketToHostPortAddress(Socket s) {
        return new HostPortAddress(s.getInetAddress().getHostName(), s.getPort());
    }

    public static String timestampNowString() {
        return Instant.now().toString();
    }

    /**
     * Resolves the passed local path of the file with the current home directory of this Handler, and gets the full
     * pathname of requested file
     *
     * @return String containing the absolute path of the two combined paths
     */
    public static Path resolveFilePath(Path mainPath, Path localPath) {
        Path resolvedPath = mainPath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }


    public static class GenericListFormatter {
        // Divide by 2, and this is the spacing on each side of each Object field
        private static final int DEFAULT_SPACING = 2;

        public static String getFormattedOutput(List<?> objectList, String delimiter, boolean includeFieldNames) {
            if (objectList.isEmpty())
                return "";
            StringBuilder formattedLine = new StringBuilder();

            int[] maxFieldLengths = getMaxFormatWidths(objectList, includeFieldNames);
            int lineLength = Arrays.stream(maxFieldLengths).reduce(0,
                    (a, b) -> a + b + delimiter.length() + DEFAULT_SPACING);
            String wrapper = StringUtils.repeat('*', lineLength) + '\n';

            // Optionally set append the variable names at the tops of the columns
            if (includeFieldNames) {
                Field[] f = objectList.get(0).getClass().getFields();
                for (int i = 0; i < f.length; i++) {
                    formattedLine.append(StringUtils.center(f[i].getName(),
                            maxFieldLengths[i] + DEFAULT_SPACING));
                    formattedLine.append(delimiter);
                }
                formattedLine.append("\n");
            }

            // Make the output a bit nicer to look at by wrapping the output
            formattedLine.append(wrapper);


            // Pump out the variable values in a formatted way
            for (Object o : objectList) {

                //formattedLine.append(delimiter);

                Field[] fields = o.getClass().getFields();
                for (int i = 0; i < fields.length; i++) {
                    try {
                        formattedLine.append(StringUtils.center(fields[i].get(o).toString(),
                                maxFieldLengths[i] + DEFAULT_SPACING));
                    } catch (IllegalAccessException e) {
                        System.out.println("Error printing out file details.");
                        e.printStackTrace();
                    }
                    formattedLine.append(delimiter);
                }
                formattedLine.append("\n");
            }
            formattedLine.append(wrapper);

            return formattedLine.toString();
        }


        private static int[] getMaxFormatWidths(List<?> objectList, boolean includeFieldNames) {
            int[] maxFieldLengths = new int[objectList.get(0).getClass().getFields().length];

            // Optionally set default values to the field name lengths
            if (includeFieldNames) {
                Field[] f = objectList.get(0).getClass().getFields();
                for (int i = 0; i < f.length; i++) {
                    maxFieldLengths[i] = f[i].getName().length();
                }
            }

            //TODO: Possibly refactor to be cleaner/using Java Streams, but this works well enough
            for (Object o : objectList) {
                Field[] fields = o.getClass().getFields();
                for (int i = 0; i < fields.length; i++) {
                    try {
                        int fieldLength = fields[i].get(o).toString().length();
                        if (maxFieldLengths[i] < fieldLength)
                            maxFieldLengths[i] = fieldLength;
                    } catch (IllegalAccessException e) {
                        System.out.println("Error printing out file details.");
                        e.printStackTrace();
                    }
                }
            }
            return maxFieldLengths;
        }
    }
}