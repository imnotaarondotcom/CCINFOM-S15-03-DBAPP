

public class Venues {

private int venue_id;
private String venue_name;
private String address;

public Venues(int venue_id,String venue_name, String address){
    this.venue_id= venue_id;
    this.venue_name=venue_name;
    this.address=address;
}

public int getVenue_id(){return venue_id;}
public String getVenue_name(){return venue_name;}
public String getAddress(){return address;}
public void setVenue_id(int venue_id){this.venue_id= venue_id;}
public void setVenue_name(String venue_name){this.venue_name= venue_name;}
public void setAddress(String address){this.address= address;}


public String formatting(){
    return String.format("ID: %d, NAME: %s, ADDRESS: %s",venue_id,venue_name,address);
}

}