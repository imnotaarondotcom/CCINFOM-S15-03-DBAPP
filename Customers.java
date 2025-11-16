public class Customers{


private int CustomerID;
private String phoneNumber;
private String Username;

public Customers(int CustomerID, String phoneNumber, String Username){
this.CustomerID=CustomerID;
this.phoneNumber=phoneNumber;
this.Username=Username;

}

public void setID(int CustomerID){
this.CustomerID=CustomerID;
}

public void setNumber(String phoneNumber){
this.phoneNumber=phoneNumber;
}

public void setName(String Username){
this.Username=Username;
}

public int getID(){
return CustomerID;
}

public String getNumber(){
return phoneNumber;
}

public String getName(){
return Username;
}

