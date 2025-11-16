import java.util.ArrayList;
import java.util.Scanner;

public class VenueManagement{
    private VenuesDao venuesDao;
    private Scanner scanner;

    public VenueManagement(Scanner scanner){
        this.venuesDao = new VenuesDao();
        this.scanner = scanner;
    }
    public void venueMenu(){
        boolean useVenue=true;
        while(useVenue){
            System.out.println("Venue Management:");
            System.out.println("1. View venues");
            System.out.println("2. Add a venue");
            System.out.println("3. Edit a venue");
            System.out.println("4. Delete a venue");
            System.out.println("5. Search venue");
            System.out.println("6. View specific venue details");
             System.out.println("7. Exit");
            System.out.print("Choice: ");
            int choice =scanner.nextInt();
            scanner.nextLine();
            switch(choice){
                case 1:
                    viewVenues();
                    break;
                case 2:
                    addVenue();
                    break;
                case 3:
                    editVenue();
                    break;
                case 4:
                    deleteVenue();
                    break;
                case 5: 
                    searchVenue();
                    break;
                case 6:
                    viewVenueDetails();
                    break;
                case 7:
                    useVenue=false;
                    break;
                default:
                    System.out.println("Invalid input, please try again.");
            }

        }
    }
    public void viewVenueDetails() {
        viewVenues();
        System.out.print("\nEnter venue ID to view details: ");
        int venueId = scanner.nextInt();
        scanner.nextLine();

        Venues venue = venuesDao.getVenueById(venueId);
        if (venue == null) {
            System.out.println("Venue not found!");
            return;
        }

        boolean viewing = true;
        while (viewing) {
            System.out.println("\nVenue Details: " + venue.getVenue_name());
            System.out.println("1. View Movies Screening Here");
            System.out.println("2. View Ticket Sales");
            System.out.println("3. View Rooms");
            System.out.println("4. View Screening Schedule");
            System.out.println("5. Back to Venue Menu");
            System.out.print("Choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    viewMoviesAtVenue(venueId);
                    break;
                case 2:
                    viewTicketSales(venueId);
                    break;
                case 3:
                    viewRoomsAtVenue(venueId);
                    break;
                case 4:
                    viewScreeningSchedule(venueId);
                    break;
                case 5:
                    viewing = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

        public ArrayList<Venues> getAllVenues(){
            return venuesDao.getAllVenues();
        }

        public Venues getVenueById(int venueId){
            return venuesDao.getVenueById(venueId);
        }

        public ArrayList<String> getRoomsByVenue(int venueId){
            return venuesDao.getRoomsByVenue(venueId);
        }

        public void viewVenues(){
            ArrayList<Venues> venues = venuesDao.getAllVenues();
            System.out.println("Displaying all venues:");
            for(Venues venue:venues){
                System.out.println(venue.formatting());
            }
        }

        public void addVenue(){
            System.out.println("Enter new name: ");
            String venue_name =scanner.nextLine();
            System.out.println("Enter new address: ");
            String address = scanner.nextLine();

            Venues newVenue = new Venues(0, venue_name, address);
            boolean added = venuesDao.addVenue(newVenue);

            if(added){
                System.out.println("A new venue has been added.");
            }
            else{
                System.out.println("Error when adding a venue.");
            }
        }

        public void deleteVenue(){

            viewVenues();
            System.out.print("Enter venue ID to delete: ");
            int venue_id= scanner.nextInt();
            boolean deleted = venuesDao.deleteVenue(venue_id);

            if (deleted){
                System.out.println("Venue has been deleted.");
            }
            else{
                System.out.println("Failed to delete venue.");
            }
        }

        public void searchVenue(){
            System.out.print("Enter name of the location: ");
            String venue_name= scanner.nextLine();
            ArrayList<Venues> searchResults = venuesDao.searchVenueName(venue_name);
            System.out.println("Showing results: ");
            if(searchResults.isEmpty()){
                System.out.println("Sorry, there are no results for: %s"+venue_name);
            }
            else{
                for(Venues venues: searchResults){
                    System.out.println(venues.formatting());
                }
            }
        }

        public void editVenue(){

            viewVenues();
            System.out.println("Which venue would you like to edit: ");
            int venue_id= scanner.nextInt();
            scanner.nextLine();

            Venues venue = venuesDao.getVenueById(venue_id);
            if(venue==null){
                System.out.println("Venue was not found.");
            }
            else{
                System.out.println("What information would you like to edit: ");
                System.out.println("1. Name");
                System.out.println("2. Address\n");
                System.out.print("Choice: ");
                int toEdit= scanner.nextInt();
                scanner.nextLine();
                
                switch(toEdit){
                    case 1:
                        System.out.print("Enter new name: ");
                        String new_name = scanner.nextLine();
                        if(!new_name.isEmpty()){
                            if(new_name.equals(venue.getVenue_name())){
                                System.out.println("ERROR: The new name cannot be the same as the old name.");
                            }
                            else{
                                ArrayList<Venues> existing = venuesDao.searchVenueName(new_name);
                                boolean same_name=false;
                                for(Venues venues: existing){
                                    if(venues.getVenue_id()!=venue_id){
                                        same_name=true;
                                    }
                                }
                                if(same_name==true){
                                    System.out.println("ERROR: Another venue already has this name.");
                                }
                                else{
                                    venue.setVenue_name(new_name);
                                    boolean updated = venuesDao.updateVenue(venue);
                                    if(updated==true){
                                        System.out.println("Venue name updated successfully.");
                                    }
                                    else{
                                        System.out.println("Failed to update venue name");
                                    }
                                }

                            }
                        }
                    break;
                    case 2:
                        System.out.print("Enter new address: ");
                        String new_address = scanner.nextLine();
                        if(!new_address.isEmpty()){
                            if(new_address.equals(venue.getAddress())){
                                System.out.println("ERROR: The new address cannot be the same as the old address.");
                            }
                            else{
                                venue.setAddress(new_address);
                                boolean updated = venuesDao.updateVenue(venue);
                                if (updated){
                                     System.out.println("Venue address updated successfully.");
                                }
                                else{
                                    System.out.println("Failed to update venue address");
                                }
                            }
                        }
                    default:
                        System.out.println("Invalid choice, please try again");
                }

            }

        }

        public void viewMoviesAtVenue(int venueId) {
            System.out.println("\nMovies Screening at This Venue:");
            ArrayList<String> movies = venuesDao.getMoviesByVenue(venueId);
            if (movies.isEmpty()) {
                System.out.println("No movies scheduled at this venue.");
            } else {
                for (String movie : movies) {
                    System.out.println("  " + movie);
                }
            }
        }

        private void viewTicketSales(int venueId) {
            System.out.println("\nTicket Sales");
            int ticketsSold = venuesDao.getTicketsSoldByVenue(venueId);
            System.out.println("Total tickets sold: " + ticketsSold);
        }

        public void viewRoomsAtVenue(int venueId) {
            System.out.println("\nRooms in This Venue: ");
            ArrayList<String> rooms = venuesDao.getRoomsByVenue(venueId);
            if (rooms.isEmpty()) {
                System.out.println("No rooms found in this venue.");
            } else {
                for (String room : rooms) {
                    System.out.println("  " + room);
                }
            }
        }

        private void viewScreeningSchedule(int venueId) {
            System.out.println("\nScreening Schedule:");
            ArrayList<String> screenings = venuesDao.getScreeningsByVenue(venueId);
            if (screenings.isEmpty()) {
                System.out.println("No screenings scheduled at this venue.");
            } else {
                for (String screening : screenings) {
                    System.out.println("  " + screening);
                }
            }
        }
}
