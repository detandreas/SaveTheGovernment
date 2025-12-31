package budget.frontend.util;

import budget.backend.model.domain.user.User;

/**
 * Singleton class to manage the logged-in user session.
 * Επιτρέπει την πρόσβαση στον τρέχοντα χρήστη από οπουδήποτε στην εφαρμογή.
 */
public final class UserSession {

    // 1. Το μοναδικό instance της κλάσης (Singleton)
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
