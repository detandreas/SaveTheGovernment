package budget.constants;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMenu {

    @Test
    void testLoginMenuHasCorrectContent() {
        List<String> menu = Menu.LOGIN_MENU;
        assertEquals(4, menu.size(), "Login menu should have 4 items");
        assertEquals("Login Menu:", menu.get(0));
        assertEquals("1. Log In", menu.get(1));
        assertEquals("2. Create New Account", menu.get(2));
        assertEquals("3. Exit Application", menu.get(3));
    }

    @Test
    void testExitConfirmationMenuHasCorrectContent() {
        List<String> menu = Menu.EXIT_CONFIRMATION_MENU;
        assertEquals(3, menu.size(), "Exit confirmation menu should have 3 items");
        assertEquals("Are you sure you want to exit the application?", menu.get(0));
        assertEquals("1. YES", menu.get(1));
        assertEquals("2. NO", menu.get(2));
    }

    @Test
    void testUpdateCredentialsMenuHasCorrectContent() {
        List<String> menu = Menu.UPDATE_CREDENTIALS_MENU;
        assertEquals(5, menu.size(), "Update credentials menu should have 5 items");
        assertEquals("Update Credentials Menu:", menu.get(0));
        assertEquals("1. Update Username", menu.get(1));
        assertEquals("2. Update Password", menu.get(2));
        assertEquals("3. Update Full Name", menu.get(3));
        assertEquals("4. Return to Main Menu", menu.get(4));
    }

    @Test
    void testRoleSelectionMenuHasCorrectContent() {
        List<String> menu = Menu.ROLE_SELECTION_MENU;
        assertEquals(4, menu.size(), "Role selection menu should have 4 items");
        assertEquals("Please select your Role:", menu.get(0));
        assertEquals("1. Citizen", menu.get(1));
        assertEquals("2. Government Member", menu.get(2));
        assertEquals("3. Prime Minister", menu.get(3));
    }

    @Test
    void testCitizenMainMenuHasCorrectContent() {
        List<String> menu = Menu.CITIZEN_MAIN_MENU;
        assertEquals(5, menu.size(), "Citizen main menu should have 5 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. View total budget", menu.get(1));
        assertEquals("2. View change history", menu.get(2));
        assertEquals("3. View statistics", menu.get(3));
        assertEquals("4. Logout.", menu.get(4));
    }

    @Test
    void testGovernmentMemberMainMenuHasCorrectContent() {
        List<String> menu = Menu.GOVERNMENT_MEMBER_MAIN_MENU;
        assertEquals(7, menu.size(), "Government member main menu should have 7 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. View total budget", menu.get(1));
        assertEquals("2. View change history", menu.get(2));
        assertEquals("3. View statistics", menu.get(3));
        assertEquals("4. Manage budget items (only edit)", menu.get(4));
        assertEquals("5. View your request history", menu.get(5));
        assertEquals("6. Logout", menu.get(6));
    }

    @Test
    void testFinanceMemberMainMenuHasCorrectContent() {
        List<String> menu = Menu.FINANCE_MEMBER_MAIN_MENU;
        assertEquals(8, menu.size(), "Finance member main menu should have 8 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. View total budget", menu.get(1));
        assertEquals("2. View change history", menu.get(2));
        assertEquals("3. View statistics", menu.get(3));
        assertEquals("4. Manage budget items (add, delete, edit)", menu.get(4));
        assertEquals("5. Manage pending change requests", menu.get(5));
        assertEquals("6. View your request history", menu.get(6));
        assertEquals("7. Logout", menu.get(7));
    }

    @Test
    void testPrimeMinisterMainMenuHasCorrectContent() {
        List<String> menu = Menu.PRIME_MINISTER_MAIN_MENU;
        assertEquals(6, menu.size(), "Prime Minister main menu should have 6 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. View total budget", menu.get(1));
        assertEquals("2. View change history", menu.get(2));
        assertEquals("3. View statistics", menu.get(3));
        assertEquals("4. Manage pending change requests", menu.get(4));
        assertEquals("5. Logout", menu.get(5));
    }

    @Test
    void testPendingRequestsSubmenuHasCorrectContent() {
        List<String> menu = Menu.PENDING_REQUESTS_SUBMENU;
        assertEquals(4, menu.size(), "Pending requests submenu should have 4 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. Approve request", menu.get(1));
        assertEquals("2. Reject request", menu.get(2));
        assertEquals("3. Return to main menu", menu.get(3));
    }

    @Test
    void testConfirmationSubmenuHasCorrectContent() {
        List<String> menu = Menu.CONFIRMATION_SUBMENU;
        assertEquals(3, menu.size(), "Confirmation submenu should have 3 items");
        assertTrue(menu.get(0).contains("Are you sure you want to submit the request"),
                "First item should contain confirmation message");
        assertTrue(menu.get(0).contains("for Prime Minister approval?"),
                "First item should contain approval message");
        assertEquals("1. YES", menu.get(1));
        assertEquals("2. NO", menu.get(2));
    }

    @Test
    void testManageBudgetItemsSubmenuHasCorrectContent() {
        List<String> menu = Menu.MANAGE_BUDGET_ITEMS_SUBMENU;
        assertEquals(5, menu.size(), "Manage budget items submenu should have 5 items");
        assertEquals("Please select an option: ", menu.get(0));
        assertEquals("1. Add budget item", menu.get(1));
        assertEquals("2. Delete budget item", menu.get(2));
        assertEquals("3. Edit budget item", menu.get(3));
        assertEquals("4. Return to main menu", menu.get(4));
    }

    @Test
    void testPasswordRequirementsHasCorrectContent() {
        List<String> menu = Menu.PASSWORD_REQUIREMENTS;
        assertEquals(7, menu.size(), "Password requirements should have 7 items");
        assertEquals("Password Requirements:", menu.get(0));
        assertTrue(menu.get(1).contains("At least 8 characters"),
                "Should contain character length requirement");
        assertTrue(menu.get(2).contains("uppercase letter"),
                "Should contain uppercase requirement");
        assertTrue(menu.get(3).contains("lowercase letter"),
                "Should contain lowercase requirement");
        assertTrue(menu.get(4).contains("digit"),
                "Should contain digit requirement");
        assertTrue(menu.get(5).contains("special character"),
                "Should contain special character requirement");
        assertTrue(menu.get(6).contains("allowed characters"),
                "Should contain allowed characters requirement");
    }
}
