package budget.model.domain.user;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import budget.model.domain.BudgetItem;
import budget.model.enums.Ministry;

public class TestCitizen {
    String username="Stelios123";
    String fullname="SteliosVeisakis";
    String password="Stelios123";
    List<Ministry> ministries = new ArrayList<>();

    BudgetItem tip=new BudgetItem(823, 2005, username, 10000,true, ministries);
    Citizen Stelios= new Citizen(username, fullname, password);

    @Test
    void TestConstructor(){
        assertEquals("Stelios123",Stelios.getUserName());
        assertEquals("SteliosVeisakis",Stelios.getFullName());
        assertEquals("Stelios123",Stelios.getHashPassword());
        assertNotNull(Stelios.getId());
    }

    @Test 
    void TestCanApprove(){
        assertFalse(Stelios.canApprove());
    }

    @Test 
    void TestCanEdit(){
        assertFalse(Stelios.canEdit(tip));
    }


    
}
