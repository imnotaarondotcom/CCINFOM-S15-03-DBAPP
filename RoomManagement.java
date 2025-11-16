import java.util.ArrayList;
import java.util.Scanner;

public class RoomManagement {
    private static RoomDao roomDao = new RoomDao();
    private static Scanner scanner = new Scanner(System.in);

    public static void manageRoom() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║      ROOM MANAGEMENT MENU          ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║ 1. Add Room                        ║");
            System.out.println("║ 2. Update Room                     ║");
            System.out.println("║ 3. Delete Room                     ║");
            System.out.println("║ 4. View All Rooms                  ║");
            System.out.println("║ 5. View Rooms with Ticket Count    ║");
            System.out.println("║ 6. View Vacant Seats (by Screening)║");
            System.out.println("║ 7. View Rooms with Movie Info      ║");
            System.out.println("║ 8. View Movie Details & Ratings    ║");
            System.out.println("║ 9. View Revenue by Room            ║");
            System.out.println("║ 10. View Capacity vs Attendance    ║");
            System.out.println("║ 11. View Total Screenings          ║");
            System.out.println("║ 0. Back to Main Menu               ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    addRoom();
                    break;
                case 2:
                    updateRoom();
                    break;
                case 3:
                    deleteRoom();
                    break;
                case 4:
                    viewAllRooms();
                    break;
                case 5:
                    viewRoomsWithTicketCount();
                    break;
                case 6:
                    viewVacantSeats();
                    break;
                case 7:
                    viewRoomsWithMovieInfo();
                    break;
                case 8:
                    viewMovieDetails();
                    break;
                case 9:
                    viewRevenueByRoom();
                    break;
                case 10:
                    viewCapacityVsAttendance();
                    break;
                case 11:
                    viewTotalScreenings();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    private static void addRoom() {
        System.out.println("\n─── ADD NEW ROOM ───");
        
        System.out.print("Enter Room Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter Room Type (e.g., IMAX, 3D, Standard): ");
        String type = scanner.nextLine().trim();
        
        System.out.print("Enter Venue ID: ");
        int venueId = getIntInput();
        
        roomDao.addRoom(name, type, venueId);
    }

    private static void updateRoom() {
        System.out.println("\n─── UPDATE ROOM ───");
        
        System.out.print("Enter Room ID to update: ");
        int roomId = getIntInput();
        
        System.out.print("Enter New Room Name: ");
        String newName = scanner.nextLine().trim();
        
        System.out.print("Enter New Room Type: ");
        String newType = scanner.nextLine().trim();
        
        roomDao.updateRoom(roomId, newName, newType);
    }

    private static void deleteRoom() {
        System.out.println("\n─── DELETE ROOM ───");
        
        System.out.print("Enter Room ID to delete: ");
        int roomId = getIntInput();
        
        System.out.print("Are you sure you want to delete this room? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("yes")) {
            roomDao.deleteRoom(roomId);
        } else {
            System.out.println("Delete operation cancelled.");
        }
    }

    private static void viewAllRooms() {
        System.out.println("\n─── ALL ROOMS ───");
        
        ArrayList<Room> rooms = roomDao.getAllRooms();
        
        if (rooms.isEmpty()) {
            System.out.println("No rooms found in the database.");
            return;
        }

        System.out.println("\n┌──────────┬────────────────────────┬────────────────────┬───────────┐");
        System.out.println("│ Room ID  │ Room Name              │ Room Type          │ Venue ID  │");
        System.out.println("├──────────┼────────────────────────┼────────────────────┼───────────┤");
        
        for (Room room : rooms) {
            System.out.printf("│ %-8d │ %-22s │ %-18s │ %-9d │%n",
                    room.getRoomId(),
                    room.getRoomName(),
                    room.getRoomType(),
                    room.getVenueId());
        }
        
        System.out.println("└──────────┴────────────────────────┴────────────────────┴───────────┘");
        System.out.println("Total rooms: " + rooms.size());
    }

    private static void viewRoomsWithTicketCount() {
        System.out.println("\n─── ROOMS WITH TICKET COUNT ───");
        
        ArrayList<String> results = roomDao.getRoomsWithTicketCount();
        
        if (results.isEmpty()) {
            System.out.println("No data available.");
            return;
        }
        
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal records: " + results.size());
    }

    private static void viewVacantSeats() {
        System.out.println("\n─── VACANT SEATS FOR SCREENING ───");
        
        System.out.print("Enter Screening ID: ");
        int screeningId = getIntInput();
        
        ArrayList<String> results = roomDao.getVacantSeatsForScreening(screeningId);
        
        if (results.isEmpty()) {
            System.out.println("No vacant seats found or screening doesn't exist.");
            return;
        }
        
        System.out.println("\nAvailable Seats:");
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal vacant seats: " + results.size());
    }

    private static void viewRoomsWithMovieInfo() {
        System.out.println("\n─── ROOMS WITH MOVIE SCHEDULES ───");
        
        ArrayList<String> results = roomDao.getRoomsWithMovieInfo();
        
        if (results.isEmpty()) {
            System.out.println("No screenings scheduled.");
            return;
        }
        
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal screenings: " + results.size());
    }

    private static void viewMovieDetails() {
        System.out.println("\n─── MOVIE DETAILS & RATINGS ───");
        
        ArrayList<String> results = roomDao.getMovieDetailsForScreenings();
        
        if (results.isEmpty()) {
            System.out.println("No movie details available.");
            return;
        }
        
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal screenings: " + results.size());
    }

    private static void viewRevenueByRoom() {
        System.out.println("\n─── REVENUE BY ROOM ───");
        
        ArrayList<String> results = roomDao.getRevenueByRoom();
        
        if (results.isEmpty()) {
            System.out.println("No revenue data available.");
            return;
        }
        
        System.out.println("\n💰 Revenue Report:");
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal rooms with revenue: " + results.size());
    }

    private static void viewCapacityVsAttendance() {
        System.out.println("\n─── VENUE CAPACITY VS ATTENDANCE ───");
        
        ArrayList<String> results = roomDao.getVenueCapacityVsAttendance();
        
        if (results.isEmpty()) {
            System.out.println("No attendance data available.");
            return;
        }
        
        System.out.println("\n📊 Occupancy Report:");
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal venues: " + results.size());
    }

    private static void viewTotalScreenings() {
        System.out.println("\n─── TOTAL SCREENINGS BY VENUE ───");
        
        ArrayList<String> results = roomDao.getTotalScreeningsByVenue();
        
        if (results.isEmpty()) {
            System.out.println("No screening data available.");
            return;
        }
        
        System.out.println("\n🎬 Screening Statistics:");
        for (String info : results) {
            System.out.println(info);
        }
        
        System.out.println("\nTotal venues: " + results.size());
    }

    private static int getIntInput() {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine().trim());
                return input;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }
}
