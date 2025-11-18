import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TicketBookingsManagement {

    private Scanner scanner;
    private TicketDao ticketDao;
    private ScreeningDao screeningDao;
    private SeatDao seatDao;
    private int loggedCustomerId;
    private ScreeningsManagement screeningsManagement;

    public TicketBookingsManagement(Scanner scanner, int customerId, ScreeningsManagement screeningsManagement) {
        this.scanner = scanner;
        this.loggedCustomerId = customerId;
        this.screeningsManagement = screeningsManagement;
        this.ticketDao = new TicketDao();
        this.screeningDao = new ScreeningDao();
        this.seatDao = new SeatDao();
    }

    // ------------------------- MENU -------------------------
    public void openMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Ticket Bookings Menu ===");

            System.out.println("1. Buy ticket");
            System.out.println("2. Refund ticket");
            System.out.println("3. Back");

            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> buyTicket();
                case 2 -> refundTicket();
                case 3 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ------------------------- BUY TICKET -------------------------
    private void buyTicket() {

        // Show screenings from ScreeningsManagement
        System.out.println("\n=== Screenings ===");
        screeningsManagement.viewAllScreenings();  

        System.out.print("\nEnter Screening ID: ");
        int screeningId = Integer.parseInt(scanner.nextLine());

        // Get available seats
        String sql = """
            SELECT seat_id, seat_no FROM Seats 
            WHERE room_id = (SELECT room_id FROM Screenings WHERE screening_id = ?)
            AND seat_id NOT IN (
                SELECT seat_id FROM TicketBookings 
                WHERE screening_id = ? AND ticket_status = 'Booked'
            )
            ORDER BY seat_no
        """;

        java.util.Map<Integer, Integer> seatMap = new java.util.HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, screeningId);
            pst.setInt(2, screeningId);
            ResultSet rs = pst.executeQuery();

            System.out.println("\nAvailable Seats:");
            while (rs.next()) {
                int seatId = rs.getInt("seat_id");
                int seatNo = rs.getInt("seat_no");
                seatMap.put(seatNo, seatId);
                System.out.print(seatNo + " ");
            }
            System.out.println();

        } catch (SQLException e) {
            System.out.println("Error retrieving seats: " + e.getMessage());
            return;
        }

        if (seatMap.isEmpty()) {
            System.out.println("No seats available for this screening.");
            return;
        }

        System.out.print("\nEnter Seat No to book: ");
        int seatNoSelected = Integer.parseInt(scanner.nextLine());

        if (!seatMap.containsKey(seatNoSelected)) {
            System.out.println("Invalid seat number.");
            return;
        }

        int seatId = seatMap.get(seatNoSelected);

        String insert = """
            INSERT INTO TicketBookings (ticket_status, seat_id, date_booked, customer_id, screening_id)
            VALUES ('Booked', ?, NOW(), ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(insert)) {

            pst.setInt(1, seatId);
            pst.setInt(2, loggedCustomerId);
            pst.setInt(3, screeningId);

            pst.executeUpdate();
            System.out.println("\nTicket booked successfully! Seat No: " + seatNoSelected);

        } catch (SQLException e) {
            System.out.println("Error booking ticket: " + e.getMessage());
        }
    }

    // ------------------------- REFUND TICKET -------------------------
    private void refundTicket() {

        String sql = """
            SELECT t.ticket_no, t.ticket_status, s.screening_date,
                s.price, m.movie_name, r.seat_no
            FROM TicketBookings t
            JOIN Screenings s ON t.screening_id = s.screening_id
            JOIN Movies m ON s.movie_id = m.movie_id
            JOIN Seats r ON t.seat_id = r.seat_id
            WHERE t.customer_id = ?
        """;

        ArrayList<Integer> customerTickets = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, loggedCustomerId);

            ResultSet rs = pst.executeQuery();

            System.out.println("\n=== Your Tickets ===");

            while (rs.next()) {
                int id = rs.getInt("ticket_no");
                customerTickets.add(id);

                System.out.printf("""
                    Ticket No: %d
                    Movie: %s
                    Seat: %d
                    Status: %s
                    Price: %.2f
                    Date: %s
                    --------------------------
                    """,
                    id,
                    rs.getString("movie_name"),
                    rs.getInt("seat_no"),
                    rs.getString("ticket_status"),
                    rs.getDouble("price"),
                    rs.getDate("screening_date")
                );
            }

            if (customerTickets.isEmpty()) {
                System.out.println("You have no tickets.");
                return;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving tickets: " + e.getMessage());
            return;
        }

        System.out.print("Enter Ticket No to refund: ");
        int ticketNo = Integer.parseInt(scanner.nextLine());

        // Check ticket status first
        String statusCheck = "SELECT ticket_status FROM TicketBookings WHERE ticket_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(statusCheck)) {

            pst.setInt(1, ticketNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("ticket_status");

                if (currentStatus.equalsIgnoreCase("Refunded")) {
                    System.out.println("This ticket has already been refunded!");
                    return;
                }
                if (currentStatus.equalsIgnoreCase("Cancelled")) {
                    System.out.println("This ticket was cancelled with the screening. Cannot refund.");
                    return;
                }
            } else {
                System.out.println("Ticket not found.");
                return;
            }

        } catch (SQLException e) {
            System.out.println("Error checking ticket status: " + e.getMessage());
            return;
        }

        // Refund
        ticketDao.refundTicket(ticketNo);

        // Inform seat availability
        String seatQuery = "SELECT seat_id FROM TicketBookings WHERE ticket_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(seatQuery)) {

            pst.setInt(1, ticketNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int seatId = rs.getInt("seat_id");
                System.out.println("Seat ID " + seatId + " is now available again.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving seat: " + e.getMessage());
        }
    }

    
}
