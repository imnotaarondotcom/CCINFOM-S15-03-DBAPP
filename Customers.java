public class Customers {

    private int customerID;
    private String phoneNumber;
    private String username;

    public Customers(int customerID, String phoneNumber, String username) {
        this.customerID = customerID;
        this.phoneNumber = phoneNumber;
        this.username = username;
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

    public int getID() {
        return customerID;
    }

    public String getNumber() {
        return phoneNumber;
    }

    public String getName() {
        return username;
    }
	
	public String formatting() {
    return String.format("ID: %d | Phone: %s | Username: %s",CustomerID, phoneNumber, Username);
}


}
