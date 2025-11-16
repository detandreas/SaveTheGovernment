package budget.service;

import budget.model.domain.user.*;
import budget.model.enums.Ministry;
import budget.model.enums.UserRole;
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

    /**
     * Registers a new user in the system.
     *
     * <p>This method performs the following operations:
     * <ul>
     *   <li>Validates that the username is not already in use.</li>
     *   <li>Hashes the plain-text password for secure storage.</li>
     *   <li>Creates a specific User subclass based on the given role.</li>
     *   <li>Saves the newly created user in the UserRepository.</li>
     * </ul>
     *
     * @param username the desired username of the new user
     * @param password the plain-text password of the new user
     * @param fullName the full name of the user
     * @param role     the role assigned to the user
     * @return true if registration succeeds, false otherwise
     */
    public boolean signUp(String username,
    String password, String fullName, UserRole role, Ministry ministry) {

        // Validate that the username is not already taken.
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return false; // Username already exists.
        }

        // Hash the password before storing it (security requirement).
        String hashedPassword = hashPassword(password);

        // Prime Minister must follow Singleton rules.
        if (role == UserRole.PRIME_MINISTER) {

            // If PM exists → reject.
            if (userRepository.primeMinisterExists()) {
                return false;
            }

            // First-time creation of Singleton PrimeMinister.
            PrimeMinister pm = PrimeMinister.getInstance(
                username,
                fullName,
                hashedPassword
            );

            userRepository.save(pm);
            return true;
        }

        // Create a new User instance according to the provided role.
        User newUser;
        switch (role) {
            case CITIZEN:
                newUser = new Citizen(username, fullName, hashedPassword);
                break;

            case GOVERNMENT_MEMBER:
                newUser =
                new GovernmentMember(username, fullName,
                hashedPassword, ministry);
                break;

            default:
                return false; // Unknown or unsupported user role.
        }

        // Save the new user using the repository (data persistence).
        userRepository.save(newUser);

        return true;
    }
}
