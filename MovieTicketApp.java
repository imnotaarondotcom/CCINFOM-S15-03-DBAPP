import java.util.Scanner;
import javax.swing.*;

public class MovieTicketApp {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
        CustomersDao customersDao = new CustomersDao();

        System.out.println("=== Welcome to Movie Ticket Management System ===");

        boolean appRunning = true;

        while (appRunning) {
            // LOGIN LOOP
            Customers loggedInUser = null;
            while (loggedInUser == null) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim();

                System.out.print("Enter password: ");
                String password = scanner.nextLine().trim();

                loggedInUser = customersDao.getCustomerByUsername(username);
                if (loggedInUser == null) {
                    System.out.println("User not found. Try again.\n");
                    continue;
                }

                if (!loggedInUser.getPassword().equals(password)) {
                    System.out.println("Incorrect password. Try again.\n");
                    loggedInUser = null;
                }
            }

            System.out.println("\nLogin successful! Welcome, " + loggedInUser.getName());

            // Show menu based on account type
            if (loggedInUser.getAccountType().equals("Admin")) {
                runAdminMenu(loggedInUser.getID(), loggedInUser.getAccountType());
            } else {
                runCustomerMenu(loggedInUser.getID(), loggedInUser.getAccountType());
            }

            // After menu ends (user logged out), ask if they want to exit app
            System.out.print("\nDo you want to exit the system? (yes/no): ");
            String exitChoice = scanner.nextLine().trim().toLowerCase();
            if (exitChoice.equals("yes") || exitChoice.equals("y")) {
                appRunning = false;
            } else {
                System.out.println("\nReturning to login...\n");
            }
        }

        scanner.close();
        System.out.println("System shutting down.");
    }


    private static void runAdminMenu(int customerId, String accountType) {
        boolean running = true;
        CustomersManagement customerManagement = new CustomersManagement(scanner);
        VenueManagement venueManagement = new VenueManagement(scanner);
        MovieManagement movieManagement = new MovieManagement(new MovieDao(), venueManagement, scanner);
        RoomManagement roomManagement = new RoomManagement();
        SeatManagement seatManagement = new SeatManagement();   
        ScreeningsManagement screeningsManagement = new ScreeningsManagement(scanner, accountType, movieManagement, venueManagement);
        TicketBookingsManagement ticketBookingsManagement = new TicketBookingsManagement(scanner, customerId, screeningsManagement);

        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Customer Management");
            System.out.println("2. Movie Management");
            System.out.println("3. Venue Management");
            System.out.println("4. Rooms Management");
            System.out.println("5. Seat Management");
            System.out.println("6. Screenings");
            System.out.println("7. Ticket Bookings");
            System.out.println("8. Logout");
            System.out.print("Choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> customerManagement.customerMenu();
                case 2 -> movieManagement.open();
                case 3 -> venueManagement.venueMenu();
                case 4 -> roomManagement.manageRoom();
                case 5 -> seatManagement.manageSeat();
                case 6 -> screeningsManagement.openMenu();
                case 7 -> ticketBookingsManagement.openMenu();
                case 8 -> running = false;
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }

    private static void runCustomerMenu(int customerId, String accountType) {
        boolean running = true;

        VenueManagement venueManagement = new VenueManagement(scanner);
        MovieManagement movieManagement = new MovieManagement(new MovieDao(), venueManagement, scanner);      
        ScreeningsManagement screeningsManagement = new ScreeningsManagement(scanner, accountType, movieManagement, venueManagement);
        TicketBookingsManagement ticketBookingsManagement = new TicketBookingsManagement(scanner, customerId, screeningsManagement);;
        ScreeningDao screeningDao = new ScreeningDao();

        while (running) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. Screenings");
            System.out.println("2. Ticket Bookings");
            System.out.println("3. View Venues");
            System.out.println("4. Logout");
            System.out.print("Choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> screeningsManagement.openMenu();
                case 2 -> ticketBookingsManagement.openMenu();
                case 3 -> {
                    System.out.println("\n=== Venues ===");
                    venueManagement.viewVenues();
                }
                case 4 -> running = false; // logout
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }

}
