package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.command.Permission;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.guard.Guard;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.data.provider.ServerProvider;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle all things permission.
 */
public class PermsCommandHandler implements CommandHandler<ServerCommandData> {

    private final Map<String, Permission> permissionNames;

    public PermsCommandHandler() {
        permissionNames = new HashMap<>();
        for (Permission p : Permission.ALL_PERMISSIONS)
            permissionNames.put(p.getName().toLowerCase(), p);
    }

    @Override
    public void handle(ServerCommandData data) {
        CommandScanner scanner = data.scanner();
        Server server = data.getServer();
        if (!scanner.hasNext()) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Permissions")
                            .setDescription(permissionMessage(data.getUser(), server, data.getServerProvider()))
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        User target = scanner.next().asUser();
        if (!scanner.hasNext()) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Permissions")
                            .setDescription(permissionMessage(target, server, data.getServerProvider()))
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        String perm = scanner.nextString();
        String val = scanner.nextString();
        final boolean value = switch (val.toLowerCase()) {
            case "allow" -> true;
            case "deny" -> false;
            default -> throw new SMABotUserException("Expected 'allow' or 'deny', found: " + val);
        };

        if (server.isOwner(target))
            throw new SMABotUserException("Owner's permission can not be changed.");
        if (server.isAdmin(target) && server.isOwner(data.getUser()))
            throw new SMABotUserException("You must be owner to set permission for admins.");
        Guard.PERMISSION_GUARD.guardSpecific(Permission.PERMISSION_SET_PERMS, data);

        if (perm.equals("all")) {
            for (Permission p : Permission.ALL_PERMISSIONS)
                data.getServerProvider().setPermission(server.getIdAsString(),
                        target.getIdAsString(), p, value);
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Permission")
                            .setDescription("Set all permissions for "
                                    + target.getNicknameMentionTag() + " to be "
                                    + (value ? "allowed" : "denied") + "!")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        Permission p = permissionNames.get(perm.toLowerCase());
        if (p == null)
            throw new SMABotUserException("Unknown permission name: " + perm);
        data.getServerProvider().setPermission(server.getIdAsString(),
                target.getIdAsString(), p, value);
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Permission")
                        .setDescription("Set permission '" + p.getName() + "' for "
                                + target.getNicknameMentionTag() + " to be "
                                + (value ? "allowed" : "denied") + "!")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

    private String permissionMessage(User user, Server server, ServerProvider provider) {
        StringBuilder msg = new StringBuilder();
        msg.append("Permissions for ")
                .append(user.getNicknameMentionTag())
                .append(":\n");
        for (Permission p : Permission.ALL_PERMISSIONS) {
            msg.append(provider.hasPermission(server, user, p) ? ":white_check_mark:" : ":x:")
                    .append(' ')
                    .append(p.getName())
                    .append('\n');
        }
        return msg.toString();
    }

}
