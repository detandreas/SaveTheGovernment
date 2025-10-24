package budget.model.user;

import budget.model.BudgetItem;

/**
 * Represents a user of the budget system.
 * Includes common fields and methods for all types of users.
 */
public  abstract class User {

    private int id;
    private String userName;
    private String fullName;
    private String password;
    private String userRole;

    /**
     *
     * @param id unique identifier
     * @param userName  user name inside budget system
     * @param fullName  user FullName
     * @param password  user password
     * @param userRole  [Citizen, GovernmentMember, PrimeMinister]
     */
    public User(
    int id,
    String userName,
    String fullName,
    String password,
    String userRole
    ) {
        this.id = id;
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
    public int getId() {
        return id;
    }
    /**
     * sets user id.
     *
     * @param id user unique identifier
     */
    public void setId(int id) {
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
     * returns user password.
     *
     * @return user password
     */
    public String getPassword() {
        return password;
    }
    /**
     * sets user password.
     *
     * @param password user password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * returns the role of the user.
     *
     * @return user Role inside budget system
     */
    public String getUserRole() {
        return userRole;
    }
    /**
     * sets user Role.
     *
     * @param userRole user Role inside the budget system
     */
    public void setUserRole(String userRole) {
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
