package budget.service;

import budget.model.user.User;
import budget.repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Service responsible for user authentication operations.
 * Handles login, logout, and password hashing.
 */
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private User currentUser;

    /**
     * Constructs a new authentication service using a given UserRepository.
     *
     * @param userRepository repository containing user data
     */
    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.currentUser = null;
    }
    /**
     * User tries to connect with username/password.
     * @return true if connection is successful, false otherwise
     */
    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(hashPassword(password))) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Disconnects current user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * returns the current connected user.
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * checks if there is connected user .
     */
    public boolean isAuthenticated() {
        return this.currentUser != null;
    }


    /**
     * Attempts to authenticate a user with the provided credentials.
     *
     * @param username the username entered by the user
     * @param password the plain text password entered by the user
     * @return true if login succeeds, false otherwise
     */
    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getHashPassword().equals(hashPassword(password))) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Returns the currently authenticated user, or null if none is logged in.
     *
     * @return the current User object, or null
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Checks whether a user is currently authenticated.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isAuthenticated() {
        return this.currentUser != null;
    }

    /**
     * Encrypts a password using SHA-256 hashing.
     *
     * @param password the plain text password
     * @return the hashed password string
     */
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error during hashing of password.", e);
        }
    }
}