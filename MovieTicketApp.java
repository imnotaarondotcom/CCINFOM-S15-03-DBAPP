import java.util.Scanner;

public class MovieTicketApp {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
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
                runAdminMenu();
            } else {
                runCustomerMenu();
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


    private static void runAdminMenu() {
        boolean running = true;
        CustomersManagement customerManagement = new CustomersManagement(scanner);
        VenueManagement venueManagement = new VenueManagement(scanner);
        MovieManagement movieManagement = new MovieManagement(new MovieDao(), venueManagement, scanner);
        RoomManagement roomManagement = new RoomManagement();
        SeatManagement seatManagement = new SeatManagement();

        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Customer Management");
            System.out.println("2. Movie Management");
            System.out.println("3. Venue Management");
            System.out.println("4. Rooms Management");
            System.out.println("5. Seat Management");
            System.out.println("6. Logout");
            System.out.print("Choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> customerManagement.customerMenu();
                case 2 -> movieManagement.open();
                case 3 -> venueManagement.venueMenu();
                case 4 -> roomManagement.manageRoom();
                case 5 -> seatManagement.manageSeat();
                case 6 -> running = false;
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }

    private static void runCustomerMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. Ticket Bookings");
            System.out.println("2. View Venues");
            System.out.println("3. View Screenings");
            System.out.println("4. View Movies");
            System.out.println("5. Logout");
            System.out.print("Choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> { /* Ticket bookings logic */ }
                case 2 -> { /* View venues logic */ }
                case 3 -> { /* View screenings logic */ }
                case 4 -> { /* View movies logic */ }
                case 5 -> running = false; // logout
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }
}
