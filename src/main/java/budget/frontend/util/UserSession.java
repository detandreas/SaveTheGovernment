package budget.frontend.util;

import budget.backend.model.domain.user.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Singleton class to manage the current user session.
 */
public final class UserSession {

    // Singleton instance
    private static UserSession instance;

    private User currentUser;

    private UserSession() {
    }
    @SuppressFBWarnings(
        value = "MS_EXPOSE_REP",
        justification =
            "Singleton pattern requires returning"
            + " the static mutable instance."
    )
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
            "UserSession must store the mutable"
            + " User object for the app lifecycle."
    )
    public void setUser(User user) {
        this.currentUser = user;
    }
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification =
            "Returning the mutable User"
            + " object is required for session management."
    )
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
