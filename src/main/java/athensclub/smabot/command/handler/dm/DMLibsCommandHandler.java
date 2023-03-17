package athensclub.smabot.command.handler.dm;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.command.handler.server.LibsCommandHandler;
import athensclub.smabot.player.SongSearcher;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class DMLibsCommandHandler implements CommandHandler<CommandData> {

    private final SongSearcher songSearcher;

    public DMLibsCommandHandler(SongSearcher songSearcher) {
        this.songSearcher = songSearcher;
    }

    @Override
    public void handle(CommandData data) {
        CommandScanner scanner = data.scanner();
        if (LibsCommandHandler.displayUserLibraryListIfNoArg(scanner, data, true))
            return;

        String action = scanner.nextString();
        if (action.equals("new")) {
            data.getUserProvider().libraryNew(data.getUser().getIdAsString());
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Library")
                            .setDescription("Successfully created new library!")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }
        //assume the command will require a library index.
        int index = scanner.next().asInt();
        if (index - 1 < 0 || index - 1 >= data.getUserProvider().libraryCount(data.getUser().getIdAsString()))
            throw new SMABotUserException("Library index out of range.");

        switch (action.toLowerCase()) {
            case "add" -> LibsCommandHandler.add(data, scanner, index, songSearcher);
            case "rr" -> LibsCommandHandler.rr(data, scanner, index);
            case "hide" -> LibsCommandHandler.hide(data, index);
            case "show" -> LibsCommandHandler.show(data, index);
            default -> throw new SMABotUserException("Unknown libs command: " + action);
        }
    }

}
