package budget.backend.model.domain;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import budget.backend.model.enums.Ministry;

public class TestBudget {
    
    private List<BudgetItem> sampleBudgetItems() {
        return List.of(
            new BudgetItem(11, 2025, "Φόροι", 1000.0, true, List.of(Ministry.FINANCE)),
            new BudgetItem(26, 2025, "Τόκοι", 3000.0, false, List.of(Ministry.FINANCE, Ministry.EDUCATION))
        );
    }

    @Nested
    class TestConstructor {

        @Test
        void constructorWithTwoParameters() {
            List<BudgetItem> items = new ArrayList<>(sampleBudgetItems());

            Budget b = new Budget(items, 2025);
            //check that year field was defined correctly
            assertEquals(2025, b.getYear(), "Failure - wrong year");
            //check that items were correctly copied
            assertEquals(items, b.getItems(), "Failure - wrong items"); //same content
            assertNotSame(items, b.getItems(), "Failure - same items"); //defensive copy
            //check default values
            assertEquals(0.0, b.getTotalRevenue(), "Failure - wrong totalRevenue");
            assertEquals(0.0, b.getTotalExpense(), "Failure - wrong totalExpense");
            assertEquals(0.0, b.getNetResult(), "Failure - wrong netResult");
        }
        @Test
        void constructorComplete() {
            List<BudgetItem> items = new ArrayList<>(sampleBudgetItems());
            Budget b = new Budget(items, 2025, 1000.0, 3000.0, -2000.0);
            //check that fields were defined correctly
            assertEquals(2025, b.getYear(), "Failure - wrong year");
            assertEquals(items, b.getItems(), "Failure - wrong items");
            assertEquals(1000.0, b.getTotalRevenue(), "Failure - wrong totalRevenue");
            assertEquals(3000.0, b.getTotalExpense(), "Failure - wrong totalExpense");
            assertEquals(-2000.0, b.getNetResult(), "Failure - wrong netResult");
            //check defensive copy
            assertNotSame(items, b.getItems(), "Failure - same items");
        }
    }

    @Nested
    class TestItemsBehavior {

        @Test
        void getItems() {
            List<BudgetItem> items = new ArrayList<>(sampleBudgetItems());
            Budget b = new Budget(items, 2025);
            List<BudgetItem> copy1 = b.getItems();
            List<BudgetItem> copy2 = b.getItems();
            //assert that internal representation is not exposed
            assertNotSame(copy1, copy2, "Failure - same copies");
            items.clear(); //does not affect field variable
            assertEquals(2, b.getItems().size(), "Failure - wrong size");
        }
        @Test
        void setItems() {
            List<BudgetItem> items = new ArrayList<>(sampleBudgetItems());
            Budget b = new Budget(items, 2025);
            List<BudgetItem> newItems = new ArrayList<>(List.of(
                new BudgetItem(22, 2025, "Infra", 300.0, true, List.of(Ministry.DEVELOPMENT, Ministry.EDUCATION)),
                new BudgetItem(27, 2025, "Edu", 150.0, false, List.of(Ministry.EDUCATION))
            ));
    
            b.setItems(newItems);
            //check if set was done correctly
            assertNotEquals(items, b.getItems(), "Failure - equal items"); //we updated the items
            assertEquals(newItems, b.getItems(), "Failure - not equal items"); //content equality
            assertNotSame(newItems, b.getItems(), "Failure - same items"); //different reference
            //assert we can't change internal field
            newItems.clear();
            assertNotEquals(0, b.getItems().size(), "Failure - wrong size");
        }
    }

    @Nested
    class TestGettersSetters {

        @Test
        void year() {
            Budget b = new Budget(List.of(), 2030);
            assertEquals(2030, b.getYear(), "Failure - wrong year");
        }
        @Test
        void totalRevenue() {
            Budget b = new Budget(List.of(), 2030);
            b.setTotalRevenue(1000.0);

            assertEquals(1000.0, b.getTotalRevenue(), "Failure - wrong totalRevenue");
        }
        @Test
        void totalExpense() {
            Budget b = new Budget(List.of(), 2030);
            b.setTotalExpense(1000.0);

            assertEquals(1000.0, b.getTotalExpense(), "Failure - wrong totalExpense");
        }
        @Test
        void netResult() {
            Budget b = new Budget(List.of(), 2030);
            b.setNetResult(1000.0);

            assertEquals(1000.0, b.getNetResult(), "Failure - wrong netResult");
        }
    }

    @Nested
    class TestToString {

        @Test
        void toStringContains() {
            Budget b = new Budget(sampleBudgetItems(), 2022, 150.5, 100.25, 50.25);
            String s = b.toString();

            assertTrue(s.contains("year=2022"), "Failure - wrong toString");
            assertTrue(s.contains("totalRevenue=150.50"), "Failure - wrong toString");
            assertTrue(s.contains("totalExpense=100.25"), "Failure - wrong toString");
            assertTrue(s.contains("netResult=50.25"), "Failure - wrong toString");
            assertTrue(s.contains("itemsCount=2"), "Failure - wrong toString");
        }
    }
}
