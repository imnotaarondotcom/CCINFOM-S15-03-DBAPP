import java.util.ArrayList;
import java.util.Scanner;

public class RoomManagement {
    private final RoomDao roomDao;
    private final Scanner scanner;

    public RoomManagement() {
        this.roomDao = new RoomDao();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n═══════════════════════════════");
            System.out.println("      ROOM MANAGEMENT SYSTEM    ");
            System.out.println("═══════════════════════════════");
            System.out.println("[1] Add Room");
            System.out.println("[2] Update Room");
            System.out.println("[3] Delete Room");
            System.out.println("[4] View All Rooms");
            System.out.println("[5] Exit");
            System.out.println("═══════════════════════════════");
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
                    System.out.println("\nExiting Room Management System. Goodbye!");
                    return;
                default:
                    System.out.println("\n❌ Invalid option! Please choose 1-5.");
            }
        }
    }

    private void addRoom() {
        System.out.println("\n─── ADD NEW ROOM ───");
        
        System.out.print("Enter Room Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Room Type: ");
        String type = scanner.nextLine();
        
        System.out.print("Enter Venue ID: ");
        int venueId = getIntInput();

        roomDao.addRoom(name, type, venueId);
    }

    private void updateRoom() {
        System.out.println("\n─── UPDATE ROOM ───");
        
        viewAllRooms();
        
        System.out.print("\nEnter Room ID to update: ");
        int roomId = getIntInput();
        
        System.out.print("Enter New Room Name: ");
        String newName = scanner.nextLine();
        
        System.out.print("Enter New Room Type: ");
        String newType = scanner.nextLine();

        roomDao.updateRoom(roomId, newName, newType);
    }

    private void deleteRoom() {
        System.out.println("\n─── DELETE ROOM ───");
        
        viewAllRooms();
        
        System.out.print("\nEnter Room ID to delete: ");
        int roomId = getIntInput();
        
        System.out.print("Are you sure you want to delete this room? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (confirm.equals("Y")) {
            roomDao.deleteRoom(roomId);
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    private void viewAllRooms() {
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

    private int getIntInput() {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine().trim());
                return input;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }

    public static void main(String[] args) {
        RoomManagement management = new RoomManagement();
        management.showMenu();
    }
}
