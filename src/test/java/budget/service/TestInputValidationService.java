package budget.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import budget.exceptions.ValidationException;
import budget.model.domain.user.User;
import budget.model.enums.UserRole;
import java.util.UUID;

public class TestInputValidationService {

    private static InputValidationService service = new InputValidationService();
    
    private static class TestUser extends User {

        public TestUser(String userName, String fullName, String password, UserRole role) {
            super(userName, fullName, password, role);
        }
        @Override
        public boolean canEdit(budget.model.domain.BudgetItem item) {
            return false;
        } 
  
        @Override
        public boolean canApprove() {
            return false;
        }

        @Override
        public boolean canSubmitChangeRequest() {
            return false;
        }
    }

    // TESTING NEW USER VALIDATION

@Test
void TestValidateNewUser(){
    final User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos ", "OASSWORD", UserRole.CITIZEN);

    assertDoesNotThrow(() -> service.validateNewUser(tUser),"Failure - valid user shouldnt throw exception");
}

@Test 
void TestValidateUserUpdate(){
  final  User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos", "OASSWORD",UserRole.PRIME_MINISTER);

  assertDoesNotThrow(() -> service.validateUserUpdate(tUser),"Failure - valid update user shouldnt throw exception");
}

@Test
void TestValidateRoleChange(){
    assertDoesNotThrow(() -> service.validateRoleChange(UserRole.CITIZEN,UserRole.GOVERNMENT_MEMBER),
    "Failure - valid role change shouldnt throw exception");
}

@Test
void validateNewUser_NullUser(){
    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(null));

    assertEquals("User is null", ex.getMessage());
}

@Test
void validateNewUser_WrongUserName(){
    final User tUser = new TestUser("WRONG USERNAME","Giannis Papadopoulos","PASSWORD",UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid username",ex.getMessage());
}

@Test
void validateNewUser_WrongFullName(){
    final User tUser = new TestUser("USER_NAME", "WRONG FULLNAME", "PASSWORD", UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid fullname",ex.getMessage());
}

@Test
void validateNewUser_WrongUserRole(){
    final User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos", "PASSWORD", null);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid user role",ex.getMessage());
}

@Test
void validateNewUser_WrongPassword(){
    final User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos",null, UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid hashed password",ex.getMessage());
}

 // TESTTING WRGONG USER UPDATE VALIDATION

@Test
void validateUserUpdate_NullUser(){
    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(null));

    assertEquals("User is null", ex.getMessage());
}

@Test
void validateUserupdate_WrongUserName(){
    final User tUser = new TestUser("WRONG USERNAME","Giannis Papadopoulos","PASSWORD",UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid username",ex.getMessage());
}

@Test
void validateUserUpdate_WrongFullName(){
    final User tUser = new TestUser("USER_NAME", "WRONG_FULLNAME", "PASSWORD", UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid fullname",ex.getMessage());
}

@Test
void validateUserUpdate_WrongUserRole(){
    final User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos", "PASSWORD", null);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid user role",ex.getMessage());
}

@Test
void validateUserUpdate_WrongPassword(){
    final User tUser = new TestUser("USER_NAME", "Giannis Papadopoulos",null, UserRole.CITIZEN);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid hashed password",ex.getMessage());
}

// TESTING WRONG ROLE CHANGE

@Test
void TestValidateRoleChange_FromNull(){
    final ValidationException ex = assertThrows(ValidationException.class,()
     -> service.validateRoleChange(null, UserRole.CITIZEN));

     assertEquals("user roles cannot be null",ex.getMessage());
}

@Test
void TestValidateRoleChange_TomNull(){
    final ValidationException ex = assertThrows(ValidationException.class,()
     -> service.validateRoleChange(UserRole.CITIZEN, null ));

     assertEquals("user roles cannot be null",ex.getMessage());
}

@Test
void TestFail(){
    ValidationException ex = assertThrows(ValidationException.class,
            () -> service.fail("error"));

    assertEquals( "error",ex.getMessage());
}
}
