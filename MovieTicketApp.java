import java.util.Scanner;

public class MovieTicketApp {

    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        boolean running = true;
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
                        CustomersManagement customerManagement = new CustomersManagement(scanner);
                        break;
                    case 2:
                        MovieMenu movieMenu = new MovieMenu(new MovieDao(), venueManagement, scanner);
                        movieMenu.open();
                        break;
                    case 3:
                        VenueManagement venueManagement = new VenueManagement(scanner);
                        venueManagement.venueMenu();
                        break;
                    case 4:
                        RoomManagement roomManagement = new RoomManagement();
                        roomManagement.showMenu();
                        break;
                    case 5:
                        SeatManagement seatManagement = new SeatManagement();
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
    
