import java.sql.*;
import java.util.ArrayList;

public class SeatDao {

    // ADD SEATS TO A ROOM
    public boolean addSeats(int roomId, int count) {
        String getMaxSeat = "SELECT COALESCE(MAX(seat_no), 0) FROM Seats WHERE room_id = ?";
        String insertSeat = "INSERT INTO Seats (room_id, seat_no) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Get starting seat number
            int startSeat = 0;
            try (PreparedStatement stmt = conn.prepareStatement(getMaxSeat)) {
                stmt.setInt(1, roomId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    startSeat = rs.getInt(1);
                }
            }

            // Insert seats
            try (PreparedStatement stmt = conn.prepareStatement(insertSeat)) {

                for (int i = 1; i <= count; i++) {
                    stmt.setInt(1, roomId);
                    stmt.setInt(2, startSeat + i);
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            return true;

        } catch (SQLException e) {
            System.out.println("Error adding seats: " + e.getMessage());
            return false;
        }
    }

    // DELETE SEAT
    public void deleteSeat(int seatId) {
        String sql = "DELETE FROM Seats WHERE seat_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, seatId);
            pst.executeUpdate();

            System.out.println("Seat deleted successfully!");

        } catch (SQLException e) {
            System.err.println("Error deleting seat: " + e.getMessage());
        }
    }

    // GET SEATS BY ROOM
    public ArrayList<Seat> getSeatsByRoom(int roomId) {
        ArrayList<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM Seats WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(new Seat(
                        rs.getInt("seat_id"),
                        rs.getString("seat_no"),
                        rs.getInt("room_id")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving seats by room: " + e.getMessage());
        }
        return list;
    }
}