public class Customers {

    private int customerID;
    private String phoneNumber;
    private String username;
    private String password;

    public Customers(int customerID, String phoneNumber, String username, String password) {
        this.customerID = customerID;
        this.phoneNumber = phoneNumber;
        this.username = username;
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
	
    public String getPassword() {
        return password;
    }

	public String formatting() {
    return String.format("ID: %d | Phone: %s | Username: %s | Password: %s",customerID, phoneNumber, username, password);
}


}
