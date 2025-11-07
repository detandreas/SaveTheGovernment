package budget.util;

import java.io.InputStream;

/**
 * Utility class providing resource paths for application files.
 * All resources are loaded from the classpath and work both in development
 * and when packaged as a JAR file.
 */
public final class PathsUtil {

    /**
     * Classpath resource path for the budget JSON file.
     */
    public static final String BUDGET_RESOURCE = "/budget.json";

    /**
     * Classpath resource path for the
     * bill-ministry mapping JSON file.
     */
    public static final String BILL_MINISTRY_MAP_RESOURCE =
                                "/bill-ministry-map.json";
    /**
     * Classpath resource path for the users JSON file.
     */
    public static final String USERS_RESOURCE =
                                "/users.json";
    /**
     * Classpath resource path for the pending-changes
     * JSON file.
     */
    public static final String PENDING_CHANGES_RESOURCE =
                                "/pending-changes.json";
    /**
     * Classpath resource path for the budget-changes
     * JSON file.
     */
    public static final String BUDGET_CHANGES_RESOURCE =
                                "/budget-changes.json";

    private PathsUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns an InputStream for the budget.json resource file.
     * Works in both development mode and when running from a JAR.
     *
     * @return InputStream for budget.json, or null if the resource is not found
     */
    public static InputStream getBudgetInputStream() {
        return PathsUtil.class.getResourceAsStream(BUDGET_RESOURCE);
    }

    /**
     * Returns an InputStream for the bill-ministry-map.json resource file.
     * Works in both development mode and when running from a JAR.
     *
     * @return InputStream for bill-ministry-map.json,
     * or null if the resource is not found
     */
    public static InputStream getBillMinistryMapInputStream() {
        return PathsUtil.class.getResourceAsStream(BILL_MINISTRY_MAP_RESOURCE);
    }
    /**
     * Returns an InputStream for the users.json resource file.
     * Works in both development mode and when running from a JAR.
     *
     * @return InputStream for users.json,
     * or null if the resource is not found
     */
    public static InputStream getUsersInputStream() {
        return PathsUtil.class.getResourceAsStream(USERS_RESOURCE);
    }
    /**
     * Returns an InputStream for the pending-changes.json resource file.
     * Works in both development mode and when running from a JAR.
     *
     * @return InputStream for pending-changes.json,
     * or null if the resource is not found
     */
    public static InputStream getPendingChangesInputStream() {
        return PathsUtil.class.getResourceAsStream(PENDING_CHANGES_RESOURCE);
    }
    /**
     * Returns an InputStream for the budget-changes.json resource file.
     * Works in both development mode and when running from a JAR.
     *
     * @return InputStream for budget-changes.json,
     * or null if the resource is not found
     */
    public static InputStream getBudgetChangesInputStream() {
        return PathsUtil.class.getResourceAsStream(BUDGET_CHANGES_RESOURCE);
    }
}
