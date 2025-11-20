import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDao {

    // Cancel a ticket
    public void cancelTicket(int ticketNo) {
        String sql = "UPDATE TicketBookings SET ticket_status = 'Cancelled' WHERE ticket_no = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, ticketNo);
            int updated = pst.executeUpdate();
            
            if (updated > 0) {
                System.out.println("Ticket cancelled successfully!");
            } else {
                System.out.println("No ticket found with ticket_no: " + ticketNo);
            }
            
        } catch (SQLException e) {
            System.err.println("Error canceling ticket: " + e.getMessage());
        }
    }

     // Refund a ticket
    public void refundTicket(int ticketNo) {
        String refundSQL = """
            UPDATE TicketBookings 
            SET ticket_status = 'Refunded' 
            WHERE ticket_no = ? AND ticket_status = 'Booked'
        """;
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // get the ticket details to know how much to subtract from revenue
                String getTicketSQL = """
                    SELECT tb.screening_id, s.price 
                    FROM TicketBookings tb
                    JOIN Screenings s ON tb.screening_id = s.screening_id
                    WHERE tb.ticket_no = ? AND tb.ticket_status = 'Booked'
                """;
                
                int screeningId = -1;
                double ticketPrice = 0;
                
                try (PreparedStatement pst = conn.prepareStatement(getTicketSQL)) {
                    pst.setInt(1, ticketNo);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            screeningId = rs.getInt("screening_id");
                            ticketPrice = rs.getDouble("price");
                        } else {
                            throw new SQLException("No booked ticket found with ticket_no: " + ticketNo);
                        }
                    }
                }
                
                // update ticket status to Refunded
                try (PreparedStatement pst = conn.prepareStatement(refundSQL)) {
                    pst.setInt(1, ticketNo);
                    int updated = pst.executeUpdate();
                    
                    if (updated > 0) {
                        System.out.println("Ticket refunded successfully! Amount: PHP " + ticketPrice);
                        conn.commit();
                    } else {
                        throw new SQLException("No ticket found with ticket_no: " + ticketNo);
                    }
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error refunding ticket: " + e.getMessage());
        }
    }
    
    // Get tickets by seat ID
    public List<Ticket> getTicketsBySeatId(int seatId) {
    List<Ticket> tickets = new ArrayList<>();
    String sql = "SELECT * FROM TicketBookings WHERE seat_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        
        pst.setInt(1, seatId);
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Ticket t = new Ticket(
                    rs.getInt("ticket_no"),
                    rs.getInt("screening_id"),
                    rs.getInt("seat_id"),
                    rs.getString("ticket_status")
                );
                tickets.add(t);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching tickets by seat: " + e.getMessage());
    }

    return tickets;
}

    // Get tickets by screening ID
    public List<Ticket> getTicketsByScreeningId(int screeningId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM TicketBookings WHERE screening_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, screeningId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket(
                        rs.getInt("ticket_no"),
                        rs.getInt("screening_id"),
                        rs.getInt("seat_id"),
                        rs.getString("ticket_status")
                    );
                    tickets.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tickets: " + e.getMessage());
        }

        return tickets;
    }

    // Update ticket status
    public void updateTicketStatus(int ticketNo, String status) {
        String sql = "UPDATE TicketBookings SET ticket_status = ? WHERE ticket_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, status);
            pst.setInt(2, ticketNo);
            int updated = pst.executeUpdate();
            
            if (updated > 0) {
                System.out.println("Ticket status updated successfully!");
            } else {
                System.out.println("No ticket found with ticket_no: " + ticketNo);
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating ticket status: " + e.getMessage());
        }
    }
}
