package athensclub.smabot.command;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Map.Entry;

/**
 * Represent a permission
 */
public class Permission {

    /**
     * A predicate that always returns true.
     */
    public static final Predicate<Entry<User, Server>> ALWAYS_ALLOWED = e -> true;

    /**
     * A predicate that returns true if and only if the given member has ADMINISTRATOR
     * permission.
     */
    private static final Predicate<Entry<User, Server>> ADMINISTRATOR_ONLY = e -> e.getValue().isAdmin(e.getKey());

    public static final Permission PERMISSION_TEST = new Permission("test", ADMINISTRATOR_ONLY);

    public static final Permission PERMISSION_BAN = new Permission("ban", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_UNBAN = new Permission("unban", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_JAIL = new Permission("jail", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_UNJAIL = new Permission("unjail", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_PREFIX = new Permission("prefix", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_LISTEN = new Permission("listen", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_NOLISTEN = new Permission("nolisten", ADMINISTRATOR_ONLY);

    public static final Permission PERMISSION_PLAY = new Permission("play", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_QUEUE = new Permission("queue", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_LOOP = new Permission("loop", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_RR = new Permission("rr", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_HELP = new Permission("help", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_SHUFFLE = new Permission("shuffle", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_ANNOUNCE = new Permission("announce", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_LIBS = new Permission("libs", ALWAYS_ALLOWED);

    public static final Permission PERMISSION_PERMS = new Permission("perms", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_SET_PERMS = new Permission("setperms", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_LEAVE = new Permission("leave", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_FORCE_LEAVE = new Permission("forceleave", ADMINISTRATOR_ONLY);
    public static final Permission PERMISSION_SKIP = new Permission("skip", ALWAYS_ALLOWED);
    public static final Permission PERMISSION_FORCE_SKIP = new Permission("forceskip", ADMINISTRATOR_ONLY);

    /**
     * An immutable list of all permissions.
     */
    public static final List<Permission> ALL_PERMISSIONS = List.of(PERMISSION_PLAY,
            PERMISSION_QUEUE,
            PERMISSION_LOOP,
            PERMISSION_HELP,
            PERMISSION_SHUFFLE,
            PERMISSION_RR,
            PERMISSION_LEAVE,
            PERMISSION_FORCE_LEAVE,
            PERMISSION_SKIP,
            PERMISSION_FORCE_SKIP,
            PERMISSION_TEST,
            PERMISSION_PERMS,
            PERMISSION_SET_PERMS,
            PERMISSION_BAN,
            PERMISSION_UNBAN,
            PERMISSION_JAIL,
            PERMISSION_UNJAIL,
            PERMISSION_PREFIX,
            PERMISSION_ANNOUNCE,
            PERMISSION_LIBS,
            PERMISSION_LISTEN,
            PERMISSION_NOLISTEN);

    private final String name;

    private final Predicate<Entry<User, Server>> defaultPolicy;

    public Permission(String name, Predicate<Entry<User, Server>> defaultPolicy) {
        this.name = name;
        this.defaultPolicy = defaultPolicy;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the permission for the given user, if the permission has not been explicitly set.
     *
     * @param member the user to find default permission.
     * @return default permission of the given user, if the permission has not been explicitly set.
     */
    public boolean defaultPermissionFor(User member, Server server) {
        return defaultPolicy.test(Map.entry(member, server));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
