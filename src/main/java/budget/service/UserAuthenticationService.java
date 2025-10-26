package budget.service;

import budget.model.user.User;
import budget.repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class UserAuthenticationService {

    private final UserRepository userRepository;
    private User currentUser;

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


