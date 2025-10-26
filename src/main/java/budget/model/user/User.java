package budget.model.user;

import budget.model.BudgetItem;
import java.util.UUID;
import budget.model.UserRole;

/**
 * Represents a user of the budget system.
 * Includes common fields and methods for all types of users.
 */
public  abstract class User {

    private UUID id;
    private String userName;
    private String fullName;
    private String password;
    private UserRole userRole;

    /**
     *
     * @param userName  user name inside budget system
     * @param fullName  user FullName
     * @param password  user password
     * @param userRole  [Citizen, GovernmentMember, PrimeMinister]
     */
    public User(
    String userName,
    String fullName,
    String password,
    UserRole userRole
    ) {
        this.id = UUID.randomUUID();
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        this.userRole = userRole;
    }

    /**
     * returns user Id.
     *
     * @return user unique identifier
     */
    public UUID getId() {
        return id;
    }
    /**
     * sets user id.
     *
     * @param id user unique identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }
    /**
     * returns userName.
     *
     * @return username of a specific user
     */
    public String getUserName() {
        return userName;
    }
    /**
     * set user UserName.
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    /**
     * returns user FullName.
     *
     * @return fullname of a specific user
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * sets user FullName.
     *
     * @param fullName full
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    /**
     * sets user password (will be hashed by service layer).
     *
     * @param password user password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * Gets the stored password hash
     * This method should only be used by authentication services.
     *
     * @return the stored has password
     */
    public String getHashPassword() {
        return password;
    }
    /**
     * returns the role of the user.
     *
     * @return user Role inside budget system
     */
    public UserRole getUserRole() {
        return userRole;
    }
    /**
     * sets user Role.
     *
     * @param userRole user Role inside the budget system
     */
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
    /**
     * checks if user can edit budgetitem based on his role.
     *
     * @param budgetItem specific fund inside the budget
     * @return if user is authorized to change budgetItem
     *
     */
    public abstract Boolean canEdit(BudgetItem budgetItem);
    /**
     * checks if user can approve a proposed change.
     *
     * @return  if user is authorized to approve a budget change
     */
    public abstract Boolean canApprove();
}
