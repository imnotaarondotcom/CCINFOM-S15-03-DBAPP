public class Customers {

    private int customerID;
    private String phoneNumber;
    private String username;
    private String accountType;
    private String password;

    public Customers(int customerID, String phoneNumber, String username, String accountType, String password) {
        this.customerID = customerID;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.accountType = accountType;
        this.password = password;
    }

    public void setID(int customerID) {
        this.customerID = customerID;
    }

    public void setNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String username) {
        this.username = username;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getID() {
        return customerID;
    }

    public String getNumber() {
        return phoneNumber;
    }

    public String getName() {
        return username;
    }
    
    public String getAccountType() {
        return accountType;
    }

    public String getPassword() {
        return password;
    }

	public String formatting() {
    return String.format("ID: %d | Phone: %s | Username: %s | Account Type: %s | Password: %s",customerID, phoneNumber, username, accountType, password);
}


}
