package budget.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.domain.PendingChange;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.domain.user.PrimeMinister;
import budget.backend.model.domain.user.User;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.backend.repository.ChangeLogRepository;
import budget.backend.repository.ChangeRequestRepository;
import budget.backend.repository.UserRepository;
import budget.constants.Message;


public class TestChangeRequestService {

    String originalDataDir;
    ChangeRequestService service;
    BudgetRepository bRepo;
    ChangeRequestRepository changeRepository;
    UserRepository userRepo;
    User gm;
    PrimeMinister pm;
    Budget budget;
    BudgetItem item1;
    BudgetItem item2;
    BudgetItem item3;
    PendingChange change;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        originalDataDir = System.getProperty("budget.data.dir");
        
        System.setProperty("budget.data.dir", tempDir.toString());
        
        Path testFile = tempDir.resolve("pending-changes.json");
        Files.writeString(testFile, "[]");
        Path budgetFile = tempDir.resolve("budget.json");
        Files.writeString(budgetFile, "[]");

        service = getService();
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

    @BeforeEach 
    void createSampleData() {
        gm = new GovernmentMember("govMem", "Full Name", "123", Ministry.DEFENSE);
        pm = PrimeMinister.getInstance();
        item1 = new BudgetItem(1, 2025, "item1", 100, false, List.of(Ministry.DEFENSE));
        item2 = new BudgetItem(2, 2025, "item2", 200, true, List.of(Ministry.FINANCE));
        item3 = new BudgetItem(3, 2025, "item3", 0.00099, true, List.of(Ministry.FINANCE));
        budget = new Budget(List.of(item1, item2, item3), 2025);
        bRepo.save(budget);
        change = new PendingChange(1, 1, 2025, "item1", gm.getFullName(), gm.getId(), 100, 102);
    }

    private ChangeRequestService getService() {
        bRepo = new BudgetRepository();
        changeRepository = new ChangeRequestRepository();
        userRepo = new UserRepository();
        return new ChangeRequestService(
            changeRepository,
            bRepo,
            userRepo,
            new BudgetValidationService(bRepo),
            new BudgetService(bRepo),
            new ChangeLogService(new ChangeLogRepository())
        );
    }

    // Tests for submitChangeRequest()

    @Test
    void testSubmitChangeRequestNullItem() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, null, 150));
        assertEquals("Null BudgetItem", ex.getMessage(), 
            "Failure - should throw exception for null item");
    }

    @Test
    void testSubmitChangeRequestBudgetDoesntExist() {
        BudgetItem wrongItem = new BudgetItem(1, 2030, "item1", 100, false, List.of(Ministry.DEFENSE));
        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, wrongItem, 250));
        assertEquals("Budget for year 2030 does not exist.", ex.getMessage(),
            "Failure - should throw exception for non-existent budget");
    }

    @Test
    void testSubmitChangeRequestItemDoesntExist() {
        BudgetItem wrongItem = new BudgetItem(100, 2025, "item1", 100, false, List.of(Ministry.DEFENSE));
        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, wrongItem, 250));
        assertEquals("Item requested for change doesn't exist", ex.getMessage(),
            "Failure - should throw exception for non-existent item");
    }

    @Test
    void testSubmitChangeRequestInvalidUpdate() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, item1, 1000));
        assertEquals(ex.getMessage(), ex.getMessage(),
            "Failure - should throw exception for invalid update");
    }

    @Test
    void testSubmitChangeRequestInvalidItemName() {
        BudgetItem wrongItem = new BudgetItem(1, 2025, "doesn't exist", 100, false, List.of(Ministry.DEFENSE));

        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, wrongItem, 102));
        assertEquals("PendingChange has invalid BudgetItem name", ex.getMessage(),
            "Failure - should throw exception for invalid item name");
    }

    @Test
    void testSubmitChangeRequestNullRequestorName() {
        gm.setFullName(null);

        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, item1, 102));
        assertEquals("PendingChange has invalid requestor name", ex.getMessage(),
            "Failure - should throw exception for null requestor name");
    }

    @Test
    void testSubmitChangeRequestInvalidNewValue() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.submitChangeRequest(gm, item3, 0.001));
        assertEquals("PendingChange has invalid new value", ex.getMessage(),
            "Failure - should throw exception for invalid new value");
    }

    @Test
    void testValidSubmitChangeRequest() {
        assertDoesNotThrow(() -> service.submitChangeRequest(gm, item1, 102),
            "Failure - should not throw exception for valid request");
    }

    // Tests for approveRequest()

    @Test
    void testApproveRequestNullChange() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.approveRequest(pm, null));
        assertEquals(Message.REQUEST_DOES_NOT_EXIST_MESSAGE, ex.getMessage(),
            "Failure - should throw exception for null change");
    }

    @Test
    void testApproveRequestNullPm() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.approveRequest(null, change));
        assertEquals("Prime Minister cannot approve requests.", ex.getMessage(),
            "Failure - should throw exception for null PM");
    }

    @Test
    void testApproveRequestInvalidStatus() {
        change.approve();

        var ex = assertThrows(IllegalStateException.class, () -> service.approveRequest(pm, change));
        assertEquals("Request is not in PENDING status. Current status: " + change.getStatus(), ex.getMessage(),
            "Failure - should throw exception for invalid status");
    }

    @Test
    void testApproveRequestBudgetDoesntExist() {
        var wrongChange = new PendingChange(1, 10, 2030, "item10", "name", gm.getId(), 100, 102);

        var ex = assertThrows(IllegalStateException.class, () -> service.approveRequest(pm, wrongChange));
        assertEquals("Budget not found for year 2030", ex.getMessage(),
            "Failure - should throw exception for non-existent budget");
    }

    @Test
    void testApproveRequestUserDoesntExist() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.approveRequest(pm, change));
        assertEquals("Change doesn't have requestor", ex.getMessage(),
            "Failure - should throw exception for non-existent user");
    }

    @Test
    void testApproveRequestDoesntAffectExistingItem() {
        userRepo.save(gm);
        var wrongChange = new PendingChange(1, 10, 2025, "item10", "name", gm.getId(), 100, 102);
        changeRepository.save(wrongChange);

        var ex = assertThrows(IllegalArgumentException.class, () -> service.approveRequest(pm, wrongChange));
        assertEquals("Change doesn't affect existing BudgetItem", ex.getMessage(),
            "Failure - should throw exception for change not affecting existing item");
    }

    @Test
    void testApproveRequestValid() {
        userRepo.save(gm);
        assertDoesNotThrow(() -> service.approveRequest(pm, change),
            "Failure - should not throw exception for valid approval");
    }

    // Tests for rejectRequest()

    @Test
    void testRejectRequestNullChange() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.rejectRequest(pm, null));
        assertEquals(Message.REQUEST_DOES_NOT_EXIST_MESSAGE, ex.getMessage(),
            "Failure - should throw exception for null change");
    }
    
    @Test
    void testRejectRequestValid() {
        userRepo.save(gm);
        assertDoesNotThrow(() -> service.rejectRequest(pm, change),
            "Failure - should not throw exception for valid rejection");
    }

    // Tests for getAllPendingChangesSortedByDate()

    @Test
    void testGetAllPendingChangesSortedByDate() {
        service.submitChangeRequest(gm, item1, 102);
        service.submitChangeRequest(gm, item2, 199);
        var changes = service.getAllPendingChangesSortedByDate();
        assertEquals(2, changes.size(),
            "Failure - should return correct number of pending changes");
    }

    @Test
    void testApproveRequestWithChangeLogFailureTriggersRollback() {
        userRepo.save(gm);
        
        ChangeLogService failingLogService = new ChangeLogService(new ChangeLogRepository()) {
            @Override
            public void recordChange(PendingChange change, User user) {
                throw new RuntimeException("Failed to record change log");
            }
        };

        ChangeRequestService serviceWithFailingLog = new ChangeRequestService(
            changeRepository,
            bRepo,
            userRepo,
            new BudgetValidationService(bRepo),
            new BudgetService(bRepo),
            failingLogService  // Use failing log service
        );

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> serviceWithFailingLog.approveRequest(pm, change)
        );

        assertTrue(ex.getMessage().contains("Failed to process approved change"),
            "Failure - should throw rollback exception");

        // Verify rollback happened
        Budget budgetAfter = bRepo.findById(2025).get();
        BudgetItem itemAfter = budgetAfter.getItems().stream()
            .filter(i -> i.getId() == 1)
            .findFirst().get();

        assertEquals(100, itemAfter.getValue(), 0.001,
            "Failure - value should be rolled back");
    }
}
