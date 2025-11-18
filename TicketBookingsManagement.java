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
    private String customerAccountType;

    public TicketBookingsManagement(Scanner scanner, int customerId, String customerAccountType) {
        this.scanner = scanner;
        this.loggedCustomerId = customerId;
        this.customerAccountType = customerAccountType;
        this.ticketDao = new TicketDao();
        this.screeningDao = new ScreeningDao();
        this.seatDao = new SeatDao();
    }

    // ------------------------- MENU -------------------------
    public void openMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Ticket Bookings Menu ===");
            if (customerAccountType.equals("Admin")) {
                System.out.println("1. Set price for screening");
                System.out.println("2. Cancel screening");
                System.out.println("3. Buy ticket");
                System.out.println("4. Refund ticket");
                System.out.println("5. Back");
            } else {
                System.out.println("1. Buy ticket");
                System.out.println("2. Refund ticket");
                System.out.println("3. Back");
            }

            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (customerAccountType.equals("Admin")) {
                switch (choice) {
                    case 1 -> setScreeningPrice();
                    case 2 -> cancelScreening();
                    case 3 -> buyTicket();
                    case 4 -> refundTicket();
                    case 5 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                switch (choice) {
                    case 1 -> buyTicket();
                    case 2 -> refundTicket();
                    case 3 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    // ------------------------- SHOW SCREENINGS -------------------------
    public void showAllScreenings() {
        String sql = """
            SELECT s.screening_id, v.venue_name, r.room_name, m.movie_name, 
                   m.genre, m.age_rating, s.screening_date, 
                   s.screening_start_time, s.screening_end_time, s.price
            FROM Screenings s
            JOIN Venues v ON s.venue_id = v.venue_id
            JOIN Rooms r ON s.room_id = r.room_id
            JOIN Movies m ON s.movie_id = m.movie_id
            ORDER BY s.screening_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            System.out.println("\n=== All Screenings ===");
            while (rs.next()) {
                System.out.printf("""
                    
                    Screening ID: %d
                    Venue: %s
                    Room: %s
                    Movie: %s
                    Genre: %s
                    Rating: %s
                    Date: %s
                    Start: %s | End: %s
                    Price: %.2f
                    ----------------------------------------
                    """,
                        rs.getInt("screening_id"),
                        rs.getString("venue_name"),
                        rs.getString("room_name"),
                        rs.getString("movie_name"),
                        rs.getString("genre"),
                        rs.getString("age_rating"),
                        rs.getDate("screening_date"),
                        rs.getTime("screening_start_time"),
                        rs.getTime("screening_end_time"),
                        rs.getDouble("price")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error showing screenings: " + e.getMessage());
        }
    }

    // ------------------------- SET SCREENING PRICE -------------------------
    private void setScreeningPrice() {
        showAllScreenings();
        System.out.print("\nEnter Screening ID to set price: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter price: ");
        double price = Double.parseDouble(scanner.nextLine());

        String sql = "UPDATE Screenings SET price = ? WHERE screening_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, price);
            statement.setInt(2, id);

            int updated = statement.executeUpdate();
            if (updated > 0) System.out.println("Price updated!");
            else System.out.println("Screening not found.");

        } catch (SQLException e) {
            System.out.println("Error updating price: " + e.getMessage());
        }
    }

    // ------------------------- BUY TICKET -------------------------
    private void buyTicket() {
        showAllScreenings();
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

        // Map seat_no -> seat_id
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
                seatMap.put(seatNo, seatId); // map seat number to id
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

        int seatId = seatMap.get(seatNoSelected); // get corresponding seat_id

        // Insert ticket (price is always from Screenings.price)
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

    // ------------------------- CANCEL SCREENING (ADMIN ONLY) -------------------------
    private void cancelScreening() {
        showAllScreenings();
        System.out.print("\nEnter Screening ID to cancel: ");
        int screeningId = Integer.parseInt(scanner.nextLine());

        Screening screening = screeningDao.getScreeningById(screeningId);
        if (screening == null) {
            System.out.println("Screening not found.");
            return;
        }

        screeningDao.updateScreeningStatus(screeningId, "Cancelled");

        List<Ticket> tickets = ticketDao.getTicketsByScreeningId(screeningId);
        for (Ticket t : tickets) {
            ticketDao.updateTicketStatus(t.getTicketNo(), "Cancelled");
        }

        System.out.println("Screening and all related tickets have been cancelled.");
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

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, loggedCustomerId);
            ResultSet rs = pst.executeQuery();

            System.out.println("\n=== Your Tickets ===");
            ArrayList<Integer> tickets = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("ticket_no");
                tickets.add(id);
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

            if (tickets.isEmpty()) {
                System.out.println("You have no tickets.");
                return;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving tickets: " + e.getMessage());
            return;
        }

        System.out.print("Enter Ticket No to refund: ");
        int ticketNo = Integer.parseInt(scanner.nextLine());

        // Check ticket status before refund
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
                    System.out.println("This ticket has been cancelled and cannot be refunded!");
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

        // Proceed with refund
        ticketDao.refundTicket(ticketNo);

        // Optional: show message that seat is available again
        String seatQuery = "SELECT seat_id FROM TicketBookings WHERE ticket_no = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(seatQuery)) {

            pst.setInt(1, ticketNo);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int seatId = rs.getInt("seat_id");
                System.out.println("Seat ID " + seatId + " is now available for booking.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving refunded seat: " + e.getMessage());
        }

        
    }

}
