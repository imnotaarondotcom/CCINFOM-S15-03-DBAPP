import java.sql.*;

public class TicketDao {

    public void cancelTicket(int ticketNo) {
        String sql = "UPDATE TicketBookings SET ticket_status = 'Canceled' WHERE ticket_no = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, ticketNo);
            pst.executeUpdate();
            
            System.out.println("Ticket canceled successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error canceling ticket: " + e.getMessage());
        }
    }

    public void refundTicket(int ticketNo) {
        String sql = "UPDATE TicketBookings SET ticket_status = 'Refunded' WHERE ticket_no = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, ticketNo);
            pst.executeUpdate();
            
            System.out.println("Ticket refunded successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error refunding ticket: " + e.getMessage());
        }
    }
}
