package com.igrium.pseudo_pbr.qc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a source engine QC file.
 */
public class QCFile {
    public static class QCCommand {
        private String commandName;
        private List<String> commandArgs = new ArrayList<>();

        public String getName() {
            return commandName;
        }

        public List<String> getCommandArgs() {
            return commandArgs;
        }

        public String getArg(int index) {
            return commandArgs.get(index);
        }

        public QCCommand(String name, String... args) {
            this(name, List.of(args));
        }

        public QCCommand(String name, List<String> args) {
            this.commandName = name;
            this.commandArgs.addAll(args);
        }

        public String serialize() {
            if (commandArgs.size() == 0) {
                return "$"+commandName;
            }
            return "$"+commandName+" "+String.join(" ", commandArgs);
        }

        public static QCCommand read(String command) throws IOException {
            command = command.strip();
            List<String> parts = splitSpaces(command);
            if (parts.size() == 0) {
                throw new IOException("QC command has no parts.");
            }

            String commandName = parts.get(0);
            return new QCCommand(commandName, parts.subList(1, parts.size()));
        }
    }

    private List<QCCommand> commands = new ArrayList<>();

    public List<QCCommand> getCommands() {
        return commands;
    }

    /**
     * Get the first instance of a command with a given name.
     * @param name The command name.
     * @return The command, or <code>null</code> if no command exists by that name.
     */
    public QCCommand getCommand(String name) {
        for (QCCommand command : commands) {
            if (command.getName().equals(name)) return command;
        }
        return null;
    }

    public void serialize(Writer writer) throws IOException {
        for (QCCommand command : commands) {
            writer.write(command.serialize());
            writer.write(System.lineSeparator());
        }
    }

    /**
     * Read a QC file.
     * @param reader File reader.
     * @return Parsed QC file.
     * @throws IOException If there's an error parsing the file.
     */
    public static QCFile read(Reader reader) throws IOException {
        QCFile file = new QCFile();
        String currentSection;
        readUntillChar(reader, '$'); // Skip to first command
        while (true) {
            currentSection = readUntillChar(reader, '$');
            if (currentSection.length() == 0) break;
            file.commands.add(QCCommand.read(currentSection));
        }
        reader.close();
        return file;
    }

    // Returns empty string if reader is finished.
    private static String readUntillChar(Reader reader, char delimiter) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        int current = 0;
        char currentChar;
        

        while (true) {
            current = reader.read();
            if (current < 0) break;

            currentChar = (char) current;
            if (currentChar == delimiter) break;

            builder.append(currentChar);
        }
        return builder.toString();
    }

    // https://stackoverflow.com/questions/7804335/split-string-on-spaces-in-java-except-if-between-quotes-i-e-treat-hello-wor
    private static List<String> splitSpaces(String in) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(in);

        while(m.find()) {
            list.add(m.group(1).replace("\"", ""));
        }
        return list;
    }
}
