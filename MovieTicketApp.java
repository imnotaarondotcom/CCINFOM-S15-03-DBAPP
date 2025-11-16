import java.util.Scanner;

public class MovieTicketApp {

    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        boolean running = true;
        CustomersManagement customerManagement = new CustomersManagement(scanner);
        VenueManagement venueManagement = new VenueManagement(scanner);
        MovieManagement movieManagement = new MovieManagement(new MovieDao(), venueManagement, scanner);
        RoomManagement roomManagement = new RoomManagement();
        SeatManagement seatManagement = new SeatManagement();
        while (running) {

                System.out.println("Movie Ticket Management System");
                System.out.println("1. Customer Management");
                System.out.println("2. Movie Management");
                System.out.println("3. Venue Management");
                System.out.println("4. Rooms Management");
                System.out.println("5. Seat Management");
                System.out.println("6. Close app \n");
                System.out.println("Choice");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch(choice){
                    case 1:
                        customerManagement.customerMenu();
                        break;
                    case 2:
                        movieManagement.open();
                        break;
                    case 3:
                        venueManagement.venueMenu();
                        break;
                    case 4:
                        roomManagement.manageRoom();
                        break;
                    case 5:
                        seatManagement.manageSeat();
                        break;
                    case 6:
                        System.out.println("System shutting down");
                        running=false;
                        break;
                    default:
                        System.out.println("Invalid input, please try again.");
                }
          
            }
            scanner.close();
        }
}
    
