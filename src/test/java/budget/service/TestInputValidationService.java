package budget.service;

import org.junit.jupiter.api.Test;

import budget.exceptions.ValidationException;
import budget.model.domain.user.User;
import budget.model.enums.UserRole;
import budget.model.domain.user.Citizen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import budget.model.domain.user.TestUser;

public class TestInputValidationService {

    private static InputValidationService service = new InputValidationService();
    
    private static Citizen tUser = new Citizen(null, null, null);


    // TESTING NEW USER VALIDATION

@Test
void TestNewUser(){
    final User tUser = new Citizen("USER_NAME", "Giannis Papadopoulos ", "OASSWORD");

    assertDoesNotThrow(() -> service.validateNewUser(tUser),"Failure - valid user shouldnt throw exception");
}

@Test 
void TestUserUpdate(){
  final  User tUser = new Citizen("USER_NAME", "Giannis Papadopoulos", "OASSWORD");

  assertDoesNotThrow(() -> service.validateUserUpdate(tUser),"Failure - valid update user shouldnt throw exception");
}

@Test
void TestRoleChange(){
    assertDoesNotThrow(() -> service.validateRoleChange(UserRole.CITIZEN,UserRole.GOVERNMENT_MEMBER),
    "Failure - valid role change shouldnt throw exception");
}

@Test
void TestNullUser(){
    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(null));

    assertEquals("User is null", ex.getMessage());
}

@Test
void TestWrongUserName(){
    final User tUser = new Citizen("WRONG USERNAME","Giannis Papadopoulos","PASSWORD");

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid username",ex.getMessage());
}

@Test
void TestWrongFullName(){
    final User tUser = new Citizen("USER_NAME", "WRONG FULLNAME", "PASSWORD");

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid fullname",ex.getMessage());
}

@Test
void TestWrongPassword(){
    final User tUser = new Citizen("USER_NAME", "Giannis Papadopoulos",null);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateNewUser(tUser));

    assertEquals("invalid hashed password",ex.getMessage());
}

 // TESTTING WRGONG USER UPDATE VALIDATION

@Test
void Test_NullUser(){
    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(null));

    assertEquals("User is null", ex.getMessage());
}

@Test
void Test_WrongUserName(){
    final User tUser = new Citizen("WRONG USERNAME","Giannis Papadopoulos","PASSWORD");

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid username",ex.getMessage());
}

@Test
void Test_WrongFullName(){
    final User tUser = new Citizen("USER_NAME", "WRONG_FULLNAME", "PASSWORD");

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid fullname",ex.getMessage());
}

@Test
void Test_WrongPassword(){
    final User tUser = new Citizen("USER_NAME", "Giannis Papadopoulos",null);

    final ValidationException ex = assertThrows(ValidationException.class,() -> service.validateUserUpdate(tUser));

    assertEquals("invalid hashed password",ex.getMessage());
}

// TESTING WRONG ROLE CHANGE

@Test
void TestFromNull(){
    final ValidationException ex = assertThrows(ValidationException.class,()
     -> service.validateRoleChange(null, UserRole.CITIZEN));

     assertEquals("user roles cannot be null",ex.getMessage());
}

@Test
void TestToNull(){
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
