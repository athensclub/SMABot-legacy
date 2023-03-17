package athensclub.smabot.command;

import athensclub.smabot.SMABotUserException;
import org.javacord.api.DiscordApi;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A scanner similar to java's {@link Scanner} class (which this implementation uses) that scan
 * through the command body and convert it to {@link CommandArgument}.
 */
public class CommandScanner {

    private final Scanner scanner;

    private final DiscordApi api;

    public CommandScanner(String body, DiscordApi api) {
        this.api = api;
        scanner = new Scanner(body + "\n"); //add new line for scanner.nextLine()
    }

    /**
     * @return The next argument in the stream, as an {@link CommandArgument} instance.
     */
    public CommandArgument next() {
        return new CommandArgument(nextString(), api);
    }

    /**
     * @return The next argument in the stream, as an {@link String} instance.
     */
    public String nextString() {
        if (!hasNext())
            throw new SMABotUserException("Too few arguments!");
        return scanner.next();
    }

    public boolean hasNext() {
        return scanner.hasNext();
    }

    /**
     * @return all the remaining text with leading whitespace trimmed.
     */
    public String remaining() {
        List<Integer> codepoints = scanner.nextLine()
                .codePoints()
                .boxed()
                .collect(Collectors.toList());
        for (int i = 0; i < codepoints.size(); i++) {
            if (!Character.isWhitespace(codepoints.get(i))) {
                StringBuilder b = new StringBuilder();
                codepoints.subList(i, codepoints.size()).forEach(b::appendCodePoint);
                return b.toString();
            }
        }
        return "";
    }
}
