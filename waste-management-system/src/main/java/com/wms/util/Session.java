package com.wms.util;

import com.wms.model.User;

/** Holds the currently logged-in user for the running application instance. */
public final class Session {
    private static User currentUser;

    private Session() { }

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static void clear() { currentUser = null; }
}
