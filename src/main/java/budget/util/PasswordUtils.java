package budget.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class PasswordUtils {
     /**
     * Encrypts a password using SHA-256 hashing.
     *
     * @param password the plain text password
     * @return the hashed password string
     */

    private PasswordUtils() {
        // Utility class - prevent instantiation
    }

    public static String hashPassword(String password) {
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
