package budget.model.domain.user;

import java.lang.reflect.Field;

import budget.model.domain.BudgetItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPrimeMinister {
    private static PrimeMinister pm;
    private static final String USER_NAME1 = "TestUser1";
    private static final String FULL_NAME1 = "Test Full Name1";
    private static final String PASSWORD1 = "TestPassword1";
    private static BudgetItem item;

    private static final String USER_NAME2 = "TestUser2";
    private static final String FULL_NAME2 = "Test Full Name 2";
    private static final String PASSWORD2 = "TestPassword2";

    @BeforeEach
        void resetSingleton() throws Exception {
            Field instanceField = PrimeMinister.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
    }

    @Test
    void testGetInstanceCreatesAndToStringContainsInfo() {
    PrimeMinister pm1 = PrimeMinister.getInstance(USER_NAME1, FULL_NAME1, PASSWORD1);
    PrimeMinister pm2 = PrimeMinister.getInstance();

    assertSame(pm1, pm2, "getInstance should return the same instance");

    String toString = pm1.toString();
    assertTrue(toString.contains("Prime Minister"), "toString should contain class name");
    }

    @Test
    void testGetInstanceNoArgsThrowsIfNotInitialized() {
        assertThrows(
        IllegalStateException.class,
        PrimeMinister::getInstance,
        "Calling getInstance() without initialization should throw IllegalStateException"
        );
    }

    @Test
        void testSingletonBehaviorKeepsFirstInitialization() {
        PrimeMinister first = PrimeMinister.getInstance(USER_NAME1, FULL_NAME1, PASSWORD1);
        PrimeMinister second = PrimeMinister.getInstance(USER_NAME2, FULL_NAME2, PASSWORD2);

        assertSame(first, second, "Both calls should return the same singleton instance");

        // toString should reflect the first initialization values (not the second)
        String s = second.toString();
        assertTrue(s.contains("userName=" + USER_NAME1), "Singleton should keep first username");
        assertTrue(s.contains("fullName=" + FULL_NAME1), "Singleton should keep first full name");
    }

    @Test
    void testPermissions() {
        PrimeMinister pm = PrimeMinister.getInstance(USER_NAME1, FULL_NAME1, PASSWORD1);

        // PrimeMinister cannot edit
        assertFalse(pm.canEdit(item), "PrimeMinister should not be able to edit any item");

        // PrimeMinister can approve
        assertTrue(pm.canApprove(), "PrimeMinister should be able to approve changes");

        // PrimeMinister cannot submit change requests
        assertFalse(pm.canSubmitChangeRequest(), "PrimeMinister should not submit change requests");
    }
}
