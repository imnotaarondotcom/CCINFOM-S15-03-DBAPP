import java.sql.*;
import java.util.ArrayList;

public class RoomDao {

    // ADD ROOM
    public void addRoom(String name, String type, int venueId) {
        String sql = "INSERT INTO Rooms(room_name, room_type, venue_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, type);
            pst.setInt(3, venueId);
            pst.executeUpdate();

            System.out.println("Room added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
        }
    }

    // UPDATE ROOM
    public void updateRoom(int roomId, String newName, String newType) {
        String sql = "UPDATE Rooms SET room_name = ?, room_type = ? WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, newName);
            pst.setString(2, newType);
            pst.setInt(3, roomId);
            pst.executeUpdate();

            System.out.println("Room updated successfully!");

        } catch (SQLException e) {
            System.err.println("Error updating room: " + e.getMessage());
        }
    }

    // DELETE ROOM
    public void deleteRoom(int roomId) {
        String sql = "DELETE FROM Rooms WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomId);
            pst.executeUpdate();

            System.out.println("Room deleted successfully!");

        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
        }
    }

    // VIEW ALL ROOMS
    public ArrayList<Room> getAllRooms() {
        ArrayList<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM Rooms";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getString("room_type"),
                        rs.getInt("venue_id")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving rooms: " + e.getMessage());
        }
        return list;
    }
}