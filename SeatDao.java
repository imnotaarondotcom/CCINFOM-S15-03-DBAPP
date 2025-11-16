import java.sql.*;
import java.util.ArrayList;

public class SeatDao {

    // ADD SEAT TO A ROOM
    public void addSeat(String seatNo, int roomId) {
        String sql = "INSERT INTO Seats(seat_no, room_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, seatNo);
            pst.setInt(2, roomId);
            pst.executeUpdate();

            System.out.println("Seat added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding seat: " + e.getMessage());
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