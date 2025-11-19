import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ScreeningDao {

    // Add a new screening (assumes room availability already checked)
    public boolean addScreening(int movieId, int venueId, int roomId, double price, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String insertSQL = """
            INSERT INTO Screenings 
            (movie_id, venue_id, room_id, price, screening_date, screening_start_time, screening_end_time, screening_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')
        """;

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(insertSQL)) {

            pst.setInt(1, movieId);
            pst.setInt(2, venueId);
            pst.setInt(3, roomId);
            pst.setDouble(4, price);
            pst.setDate(5, Date.valueOf(date));
            pst.setTime(6, Time.valueOf(startTime));
            pst.setTime(7, Time.valueOf(endTime));

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding screening: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> getRoomsByVenue(int venueId) {
        return new ArrayList<>();
    }

    public void updateScreeningStatus(int screeningId, String status) {
        String command = "UPDATE Screenings SET screening_status = ? WHERE screening_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setString(1, status);
            statement.setInt(2, screeningId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get a single Screening by ID
    public Screening getScreeningById(int screeningId) {
        String command = "SELECT * FROM Screenings WHERE screening_id = ?";

        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setInt(1, screeningId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Screening(
                            rs.getInt("screening_id"),
                            rs.getInt("movie_id"),
                            rs.getInt("venue_id"),
                            rs.getInt("room_id"),
                            rs.getDouble("price"),                      // price
                            rs.getDate("screening_date").toLocalDate(),
                            rs.getTime("screening_start_time").toLocalTime(),
                            rs.getTime("screening_end_time").toLocalTime(),
                            rs.getString("screening_status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching screening: " + e.getMessage());
        }

        return null;
    }

    public boolean isRoomAvailable(int roomId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer excludeScreeningId) {
        String sql = """
            SELECT COUNT(*) FROM Screenings
            WHERE room_id = ? AND screening_date = ?
            AND ((screening_start_time <= ? AND screening_end_time > ?)
                OR (screening_start_time < ? AND screening_end_time >= ?))
        """;

        if (excludeScreeningId != null) {
            sql += " AND screening_id <> ?";
        }

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomId);
            pst.setDate(2, Date.valueOf(date));
            pst.setTime(3, Time.valueOf(startTime));
            pst.setTime(4, Time.valueOf(startTime));
            pst.setTime(5, Time.valueOf(endTime));
            pst.setTime(6, Time.valueOf(endTime));

            if (excludeScreeningId != null) {
                pst.setInt(7, excludeScreeningId);
            }

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // true if no conflict
            }

        } catch (SQLException e) {
            System.out.println("Error checking room availability: " + e.getMessage());
        }

        return false;
    }

    public boolean cancelScreeningAndTickets(int screeningId) {
        String cancelTicketsSQL = "UPDATE TicketBookings SET ticket_status = 'Cancelled' WHERE screening_id = ? AND ticket_status = 'Booked'";
        String cancelScreeningSQL = "UPDATE Screenings SET screening_status = 'Cancelled' WHERE screening_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Cancel all booked tickets
                try (PreparedStatement pst = conn.prepareStatement(cancelTicketsSQL)) {
                    pst.setInt(1, screeningId);
                    pst.executeUpdate();
                }
                
                // Cancel the screening
                try (PreparedStatement pst = conn.prepareStatement(cancelScreeningSQL)) {
                    pst.setInt(1, screeningId);
                    pst.executeUpdate();
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.out.println("Error cancelling screening and tickets: " + e.getMessage());
            return false;
        }
    }

    

}
