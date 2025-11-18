package budget.service;

import budget.model.domain.user.User;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.enums.Ministry;
import budget.model.enums.UserRole;
import budget.repository.UserRepository;
import budget.util.PasswordUtils;
import java.util.Optional;
import java.util.HexFormat;
import java.security.MessageDigest;

/**
 * Service responsible for user authentication operations.
 * Handles login, logout, and password hashing.
 */
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private User currentUser;
    private static final HexFormat HEX_FORMATTER = HexFormat.of();
    private static final byte[] DUMMY_SHA256
                        = HEX_FORMATTER.parseHex(PasswordUtils.hashPassword("dummy"));

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
        String normalizedUsername = (username == null) ? "" : username.trim();
        if (password == null || password.isEmpty()) {
            // Dummy operation για σταθερό χρόνο
            MessageDigest.isEqual(DUMMY_SHA256, DUMMY_SHA256);
            return false;
        }
        String candidateHex = PasswordUtils.hashPassword(password);
        Optional<User> userOpt = userRepository.findByUsername(normalizedUsername);
        
        try {
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String stored = user.getHashPassword();
                
                if (stored != null && !stored.isEmpty()) {
                    byte[] storedBytes = HEX_FORMATTER.parseHex(stored);
                    byte[] candidateBytes = HEX_FORMATTER.parseHex(candidateHex);
                    
                    if (MessageDigest.isEqual(storedBytes, candidateBytes)) {
                        this.currentUser = user;
                        return true;
                    }
                }
                // Dummy compare για σταθερό χρόνο
                MessageDigest.isEqual(DUMMY_SHA256, HEX_FORMATTER.parseHex(candidateHex));
                return false;
            } else {
                // Dummy compare για αποφυγή user enumeration
                MessageDigest.isEqual(DUMMY_SHA256, HEX_FORMATTER.parseHex(candidateHex));
                return false;
            }
        } catch (IllegalArgumentException e) {
            // Invalid hex format - treat as failed login
            // Dummy operation για σταθερό χρόνο
            MessageDigest.isEqual(DUMMY_SHA256, DUMMY_SHA256);
            return false;
        }
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
     * Registers a new user in the system.
     *
     * This method performs the following operations:
     *   Validates that the username is not already in use.
     *   Hashes the plain-text password for secure storage.
     *   Creates a specific User subclass based on the given role.
     *   Saves the newly created user in the UserRepository.
     *
     * @param username the desired username of the new user
     * @param password the plain-text password of the new user
     * @param fullName the full name of the user
     * @param role     the role assigned to the user
     * @param ministry the user's ministry
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
        String hashedPassword = PasswordUtils.hashPassword(password);

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
