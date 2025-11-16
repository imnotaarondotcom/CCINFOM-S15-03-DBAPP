import java.util.ArrayList;
import java.util.Scanner;

public class SeatManagement {
    private static SeatDao seatDao = new SeatDao();
    private static Scanner scanner = new Scanner(System.in);

    public static void manageSeat() {
        while (true) {
            System.out.println("\n===== SEAT MANAGEMENT =====");
            System.out.println("1. Add Seat");
            System.out.println("2. Delete Seat");
            System.out.println("3. View Seats by Room");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addSeat();
                    break;
                case 2:
                    deleteSeat();
                    break;
                case 3:
                    viewSeatsByRoom();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    private static void addSeat() {
        System.out.print("Enter Seat Number: ");
        String seatNo = scanner.nextLine();

        System.out.print("Enter Room ID: ");
        int roomId = scanner.nextInt();
        scanner.nextLine();

        seatDao.addSeat(seatNo, roomId);
    }

    private static void deleteSeat() {
        System.out.print("Enter Seat ID to delete: ");
        int seatId = scanner.nextInt();
        scanner.nextLine();

        seatDao.deleteSeat(seatId);
    }

    private static void viewSeatsByRoom() {
        System.out.print("Enter Room ID: ");
        int roomId = scanner.nextInt();
        scanner.nextLine();

        ArrayList<Seat> seats = seatDao.getSeatsByRoom(roomId);

        if (seats.isEmpty()) {
            System.out.println("No seats found for this room.");
        } else {
            System.out.println("\n--- Seats in Room ID: " + roomId + " ---");
            System.out.printf("%-10s %-15s %-10s%n", "Seat ID", "Seat Number", "Room ID");
            System.out.println("-------------------------------------------");
            for (Seat seat : seats) {
                System.out.printf("%-10d %-15s %-10d%n",
                        seat.getSeatId(),
                        seat.getSeatNo(),
                        seat.getRoomId());
            }
        }
    }
}
