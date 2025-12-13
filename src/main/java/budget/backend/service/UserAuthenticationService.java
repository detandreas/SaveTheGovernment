package budget.backend.service;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import budget.backend.exceptions.UserNotAuthorizedException;
import budget.backend.exceptions.ValidationException;
import budget.backend.model.domain.user.Citizen;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.model.enums.UserRole;
import budget.backend.repository.UserRepository;
import budget.backend.util.PasswordUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Service responsible for user authentication operations.
 * Handles login, logout, and password hashing.
 */
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private User currentUser;
    private static final HexFormat HEX_FORMATTER = HexFormat.of();
    private static final byte[] DUMMY_SHA256
                        = HEX_FORMATTER
                            .parseHex(PasswordUtils.hashPassword("dummy"));

    /**
     * Constructs a new authentication service using a given UserRepository.
     *
     * @param userRepository repository containing user data
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification
        = "This allows testability and shared state across service instances."
    )
    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.currentUser = null;
    }

    /**
     * Attempts to authenticate a user with the provided credentials.
     *
     * @param username the username entered by the user
     * @param password the plain text password entered by the user
     * @throws ValidationException if the password is null or empty
     * @throws UserNotAuthorizedException if authentication fails
     */
    public void login(String username, String password) {
        String normalizedUsername = (username == null) ? "" : username.trim();
        if (password == null || password.isEmpty()) {
            // Dummy operation για σταθερό χρόνο
            MessageDigest.isEqual(DUMMY_SHA256, DUMMY_SHA256);
            throw new ValidationException(
                                "Ο κωδικός πρόσβασης είναι υποχρεωτικός.");
        }
        String candidateHex = PasswordUtils.hashPassword(password);
        Optional<User> userOpt = userRepository
                                        .findByUsername(normalizedUsername);
        try {
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String stored = user.getHashPassword();

                if (stored != null && !stored.isEmpty()) {
                    byte[] storedBytes = HEX_FORMATTER.parseHex(stored);
                    byte[] candidateBytes
                                    = HEX_FORMATTER.parseHex(candidateHex);

                    if (MessageDigest.isEqual(storedBytes, candidateBytes)) {
                        this.currentUser = user;
                        return;
                    }
                }
                // Dummy compare για σταθερό χρόνο
                MessageDigest.isEqual(DUMMY_SHA256,
                                    HEX_FORMATTER.parseHex(candidateHex));
                throw new UserNotAuthorizedException("Λάθος στοιχεία.");
            } else {
                // Dummy compare για αποφυγή user enumeration
                MessageDigest.isEqual(DUMMY_SHA256,
                                    HEX_FORMATTER.parseHex(candidateHex));
                throw new UserNotAuthorizedException("Λάθος στοιχεία.");
            }
        } catch (IllegalArgumentException e) {
            // Invalid hex format - treat as failed login
            // Dummy operation για σταθερό χρόνο
            MessageDigest.isEqual(DUMMY_SHA256, DUMMY_SHA256);
            throw new UserNotAuthorizedException("Λάθος στοιχεία.");
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
     @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "currentUser instance should be accessible."
    )
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
     * @throws ValidationException if any validation fails
     */
    public void signUp(String username,
    String password, String fullName, UserRole role, Ministry ministry) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Το όνομα χρήστη είναι υποχρεωτικό.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException(
                                    "Ο κωδικός πρόσβασης είναι υποχρεωτικός.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Το πλήρες όνομα είναι υποχρεωτικό.");
        }
        if (role == null) {
            throw new ValidationException("Ο ρόλος χρήστη είναι υποχρεωτικός.");
        }
        if (role == UserRole.GOVERNMENT_MEMBER && ministry == null) {
            throw new ValidationException(
                        "Το υπουργείο είναι υποχρεωτικό για μέλη κυβέρνησης.");
        }
        username = username.trim();
        // Validate that the username is not already taken.
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new ValidationException("Το όνομα χρήστη υπάρχει ήδη.");
        }

        // Hash the password before storing it (security requirement).
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Prime Minister must follow Singleton rules.
        if (role == UserRole.PRIME_MINISTER) {
            // If PM exists → reject.
            if (userRepository.primeMinisterExists()) {
                throw new ValidationException(
                                    "Υπάρχει ήδη Πρωθυπουργός στο σύστημα.");
            }
            // First-time creation of Singleton PrimeMinister.
            PrimeMinister pm = PrimeMinister.getInstance(
                username,
                fullName,
                hashedPassword
            );
            userRepository.save(pm);
            return;
        }

        // Create a new User instance according to the provided role.
        User newUser =
        switch (role) {
            case CITIZEN ->
                new Citizen(username, fullName, hashedPassword);
            case GOVERNMENT_MEMBER ->
                new GovernmentMember(username, fullName,
                                    hashedPassword, ministry);
            default -> null; // Unknown or unsupported user role.
        };

        // Save the new user using the repository (data persistence).
        userRepository.save(newUser);
    }
}
