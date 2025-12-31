package budget.frontend.util;

import budget.backend.model.domain.user.User;

/**
 * Singleton class to manage the current user session.
 */
public final class UserSession {

    // Singleton instance
    private static UserSession instance;

    private User currentUser;

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public User getUser() {
        return currentUser;
    }

    public void cleanUserSession() {
        currentUser = null;
    }

    @Override
    public String toString() {
        return "UserSession{" + "currentUser=" + currentUser + '}';
    }
}
