import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ScreeningDao {

    // Add screening with price and status
    public boolean addScreening(int movieId, int venueId, int roomId, double price, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String checkCommand = """
            SELECT COUNT(*) FROM Screenings 
            WHERE room_id = ? AND screening_date = ? 
              AND ((screening_start_time <= ? AND screening_end_time > ?) 
                   OR (screening_start_time < ? AND screening_end_time >= ?))
            """;

        String insertCommand = """
            INSERT INTO Screenings 
            (movie_id, venue_id, room_id, price, screening_date, screening_start_time, screening_end_time, screening_status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkCommand);
             PreparedStatement insertStmt = connection.prepareStatement(insertCommand)) {

            // Check for time conflict
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
            insertStmt.setDouble(4, price); // <-- set price
            insertStmt.setDate(5, Date.valueOf(date));
            insertStmt.setTime(6, Time.valueOf(startTime));
            insertStmt.setTime(7, Time.valueOf(endTime));

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

}
