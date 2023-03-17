package athensclub.smabot.manager;

import athensclub.smabot.command.Permission;

import java.util.HashMap;

public class PermissionManager {

    private final HashMap<String, Permission> permissions;

    public PermissionManager() {
        permissions = new HashMap<>();
    }

    /**
     * Add the permission to the mapping.
     *
     * @param perm the permission to add.
     */
    public void add(Permission perm) {
        permissions.put(perm.getName().toLowerCase(), perm);
    }

    /**
     * Get {@link Permission} instance from its name.
     *
     * @param name the name of the permission (case-insensitive).
     * @return {@link Permission} instance with the given name.
     */
    public Permission getPermission(String name) {
        return permissions.get(name.toLowerCase());
    }
}
