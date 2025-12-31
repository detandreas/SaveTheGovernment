package budget.frontend.util;

import budget.backend.model.domain.user.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Singleton class to manage the current user session.
 */
public final class UserSession {

    // Singleton instance
    private static UserSession instance;
    /**
     * The currently logged-in user associated with this session.
     * Is {@code null} if no user is logged in.
     */
    private User currentUser;

    private UserSession() {
    }
    /**
     * Retrieves the single, thread-safe instance of {@code UserSession}.
     * Implements lazy initialization: the instance is created only when
     * this method is called for the first time.
     *
     * @return the singleton instance of UserSession
     */
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
    /**
     * Sets the currently logged-in user for this session.
     * This method is typically called upon successful authentication.
     * @param user the {@link User} object to store in the session
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
            "UserSession must store the mutable"
            + " User object for the app lifecycle."
    )
    public void setUser(User user) {
        this.currentUser = user;
    }
    /**
     * Retrieves the currently logged-in user.
     *
     * @return the current {@link User} object,
     * or {@code null} if no user is logged in
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification =
            "Returning the mutable User"
            + " object is required for session management."
    )
    public User getUser() {
        return currentUser;
    }
    /**
     * Clears the current user session.
     * This method effectively logs out the user
     * by setting the current user reference to {@code null}.
     */
    public void cleanUserSession() {
        currentUser = null;
    }
    /**
     * Returns a string representation of the UserSession.
     * @return a string containing the current user's details
     */
    @Override
    public String toString() {
        return "UserSession{" + "currentUser=" + currentUser + '}';
    }
}
