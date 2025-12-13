package budget.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility helper for locating JSON resources. Preferentially resolves writable
 * files from an external data directory and transparently falls back to the
 * classpath copy when no external file is available.
 */
public final class PathsUtil {
    private static final Logger LOGGER =
                    Logger.getLogger(PathsUtil.class.getName());
    private static final String DATA_DIR_PROPERTY = "budget.data.dir";

    private static final String BUDGET_FILE = "budget.json";
    private static final String BILL_MINISTRY_MAP_FILE =
                                    "bill-ministry-map.json";
    private static final String USERS_FILE = "users.json";
    private static final String PENDING_CHANGES_FILE = "pending-changes.json";
    private static final String BUDGET_CHANGES_FILE = "budget-changes.json";

    // Classpath resource
    public static final String BUDGET_RESOURCE = "/" + BUDGET_FILE;
    public static final String BILL_MINISTRY_MAP_RESOURCE =
                                    "/" + BILL_MINISTRY_MAP_FILE;
    public static final String USERS_RESOURCE = "/" + USERS_FILE;
    public static final String PENDING_CHANGES_RESOURCE =
                                    "/" + PENDING_CHANGES_FILE;
    public static final String BUDGET_CHANGES_RESOURCE =
                                    "/" + BUDGET_CHANGES_FILE;

    private PathsUtil() {
        // Utility class - prevent instantiation
    }
    /**
     * Resolves the target {@link Path} for a data file. If the system property
     * {@code budget.data.dir} is defined, it is used as the base directory;
     * otherwise the method falls back to {@code src/main/resources}.
     *
     * @param fileName the file name to resolve
     * @return the effective {@link Path} pointing to the file
     */
    public static Path resolveDataFile(final String fileName) {
        String dataDir = System.getProperty(DATA_DIR_PROPERTY);
        if (dataDir != null && !dataDir.isBlank()) {
            Path base = Paths.get(dataDir);
            return Files.isDirectory(base) ? base.resolve(fileName) : base;
        }
        return Paths.get("src", "main", "resources", fileName);
    }

    /**
     * Attempts to open an {@link InputStream} from the external data directory
     * and falls back to the classpath resource if the file is missing or not
     * readable.
     *
     * @param fileName     the file name in the data directory
     * @param resourcePath the classpath resource path
     * @return an {@link InputStream} for the external or classpath copy
     */
    public static InputStream openDataStream(
        final String fileName,
        final String resourcePath
    ) {
        Path externalFile = resolveDataFile(fileName);
        if (Files.exists(externalFile) && Files.isReadable(externalFile)
            && !Files.isDirectory(externalFile)) {
            try {
                return Files.newInputStream(externalFile);
            } catch (IOException e) {
                LOGGER.log(
                    Level.WARNING,
                    "Failed to load external file " + fileName,
                    e
                );
            }
        }
        return PathsUtil.class.getResourceAsStream(resourcePath);
    }

    /**
     * Returns the writable {@link Path} for the budget JSON file.
     *
     * @return the path pointing to {@code budget.json}
     */
    public static Path getBudgetWritablePath() {
        return resolveDataFile(BUDGET_FILE);
    }

    /**
     * Returns the writable {@link Path} for the bill-ministry map JSON file.
     *
     * @return the path pointing to {@code bill-ministry-map.json}
     */
    public static Path getBillMinistryWritablePath() {
        return resolveDataFile(BILL_MINISTRY_MAP_FILE);
    }

    /**
     * Returns the writable {@link Path} for the users JSON file.
     *
     * @return the path pointing to {@code users.json}
     */
    public static Path getUsersWritablePath() {
        return resolveDataFile(USERS_FILE);
    }

    /**
     * Returns the writable {@link Path} for the pending changes JSON file.
     *
     * @return the path pointing to {@code pending-changes.json}
     */
    public static Path getPendingChangesWritablePath() {
        return resolveDataFile(PENDING_CHANGES_FILE);
    }

    /**
     * Returns the writable {@link Path} for the budget changes JSON file.
     *
     * @return the path pointing to {@code budget-changes.json}
     */
    public static Path getBudgetChangesWritablePath() {
        return resolveDataFile(BUDGET_CHANGES_FILE);
    }

    /**
     * Loads the budget JSON stream from the external data directory if present,
     * otherwise from the classpath resource.
     *
     * @return an {@link InputStream} for {@code budget.json}, or {@code null}
     *         when the resource cannot be found
     */
    public static InputStream getBudgetInputStream() {
        return openDataStream(BUDGET_FILE, BUDGET_RESOURCE);
    }

    /**
     * Loads the bill-ministry map JSON stream from the external data directory
     * if present, otherwise from the classpath resource.
     *
     * @return an {@link InputStream} for {@code bill-ministry-map.json}, or
     *         {@code null} when the resource cannot be found
     */
    public static InputStream getBillMinistryMapInputStream() {
        return openDataStream(
            BILL_MINISTRY_MAP_FILE,
            BILL_MINISTRY_MAP_RESOURCE
            );
    }

    /**
     * Loads the users JSON stream from the external data directory if present,
     * otherwise from the classpath resource.
     *
     * @return an {@link InputStream} for {@code users.json}, or {@code null}
     *         when the resource cannot be found
     */
    public static InputStream getUsersInputStream() {
        return openDataStream(USERS_FILE, USERS_RESOURCE);
    }

    /**
     * Loads the pending changes JSON stream from the external data directory if
     * present, otherwise from the classpath resource.
     *
     * @return an {@link InputStream} for {@code pending-changes.json}, or
     *         {@code null} when the resource cannot be found
     */
    public static InputStream getPendingChangesInputStream() {
        return openDataStream(PENDING_CHANGES_FILE, PENDING_CHANGES_RESOURCE);
    }

    /**
     * Loads the budget changes JSON stream from the external data directory if
     * present, otherwise from the classpath resource.
     *
     * @return an {@link InputStream} for {@code budget-changes.json}, or
     *         {@code null} when the resource cannot be found
     */
    public static InputStream getBudgetChangesInputStream() {
        return openDataStream(BUDGET_CHANGES_FILE, BUDGET_CHANGES_RESOURCE);
    }
}
