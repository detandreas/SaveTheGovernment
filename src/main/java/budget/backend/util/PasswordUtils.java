package budget.backend.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final  class PasswordUtils {
    private PasswordUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Encrypts a password using SHA-256 hashing.
     *
     * @param password the plain text password
     * @return the hashed password string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
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
