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

