package budget.repository;

import budget.model.user.User;
import budget.model.user.UserRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing user data.
 * Handles loading, saving, and validation of users from JSON storage.
 */
public class UserRepository {