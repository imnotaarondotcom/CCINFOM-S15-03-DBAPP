import java.util.Scanner;

public class MovieTicketApp {

    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        boolean running = true;
        VenueManagement venueManagement = new VenueManagement(scanner);
        while (running) {

                System.out.println("Movie Ticket Management System");
                System.out.println("1. Venues");
                System.out.println("2. Movie Management");
                System.out.println("3. Customer Management");
                System.out.println("4. Close app \n");
                System.out.println("Choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch(choice){
                    case 1:
                        venueManagement.venueMenu();
                        break;

                    case 4:
                        System.out.println("System shutting down");
                        running=false;
                        break;
                    default:
                        System.out.println("Invalid input, please try again.");
                }
          
            }
        }
}
    