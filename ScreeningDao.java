import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ScreeningDao {

    public boolean addScreening(int movieId, int venueId, int roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String checkCommand = "SELECT COUNT(*) FROM Screenings WHERE room_id = ? AND screening_date = ? AND " +
                              "((screening_start_time <= ? AND screening_end_time > ?) OR " +
                              "(screening_start_time < ? AND screening_end_time >= ?))";

        String insertCommand = "INSERT INTO Screenings (movie_id, venue_id, room_id, screening_date, screening_start_time, screening_end_time) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkCommand);
             PreparedStatement insertStmt = conn.prepareStatement(insertCommand)) {

            // Check for conflicting screenings
            checkStmt.setInt(1, roomId);
            checkStmt.setDate(2, Date.valueOf(date));
            checkStmt.setTime(3, Time.valueOf(startTime));
            checkStmt.setTime(4, Time.valueOf(startTime));
            checkStmt.setTime(5, Time.valueOf(endTime));
            checkStmt.setTime(6, Time.valueOf(endTime));

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Conflict exists
            }

            // Insert screening
            insertStmt.setInt(1, movieId);
            insertStmt.setInt(2, venueId);
            insertStmt.setInt(3, roomId);
            insertStmt.setDate(4, Date.valueOf(date));
            insertStmt.setTime(5, Time.valueOf(startTime));
            insertStmt.setTime(6, Time.valueOf(endTime));

            int added = insertStmt.executeUpdate();
            return added > 0;

        } catch (SQLException e) {
            System.out.println("Error adding screening: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> getRoomsByVenue(int venueId) {
        return new ArrayList<>();
    }
}
