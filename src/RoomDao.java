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
    public boolean deleteRoom(int roomId) {
        String deleteTicketsFromScreeningsSQL = """
            DELETE tb FROM TicketBookings tb 
            JOIN Screenings s ON tb.screening_id = s.screening_id 
            WHERE s.room_id = ?
        """;
        String deleteTicketsFromSeatsSQL = """
            DELETE tb FROM TicketBookings tb 
            JOIN Seats se ON tb.seat_id = se.seat_id 
            WHERE se.room_id = ?
        """;
        String deleteScreeningsSQL = "DELETE FROM Screenings WHERE room_id = ?";
        String deleteSeatsSQL = "DELETE FROM Seats WHERE room_id = ?";
        String deleteRoomSQL = "DELETE FROM Rooms WHERE room_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // delete all tickets for screenings in this room
                try (PreparedStatement pst = conn.prepareStatement(deleteTicketsFromScreeningsSQL)) {
                    pst.setInt(1, roomId);
                    pst.executeUpdate();
                }
                
                // delete all tickets that reference seats in this room
                try (PreparedStatement pst = conn.prepareStatement(deleteTicketsFromSeatsSQL)) {
                    pst.setInt(1, roomId);
                    pst.executeUpdate();
                }
                
                // delete all screenings in this room
                try (PreparedStatement pst = conn.prepareStatement(deleteScreeningsSQL)) {
                    pst.setInt(1, roomId);
                    pst.executeUpdate();
                }
                
                // delete all seats in this room
                try (PreparedStatement pst = conn.prepareStatement(deleteSeatsSQL)) {
                    pst.setInt(1, roomId);
                    pst.executeUpdate();
                }
                
                // delete the room
                try (PreparedStatement pst = conn.prepareStatement(deleteRoomSQL)) {
                    pst.setInt(1, roomId);
                    int updated = pst.executeUpdate();
                    conn.commit();
                    return updated > 0;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
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

    // VIEW ROOMS WITH TICKET BOOKING COUNT
    public ArrayList<String> getRoomsWithTicketCount() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_name, r.room_type, COUNT(tb.ticket_no) AS ticket_count " +
                    "FROM Rooms r " +
                    "LEFT JOIN Screenings s ON r.room_id = s.room_id " +
                    "LEFT JOIN TicketBookings tb ON s.screening_id = tb.screening_id " +
                    "WHERE tb.ticket_status = 'Booked' OR tb.ticket_status IS NULL " +
                    "GROUP BY r.room_id, r.room_name, r.room_type";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String info = String.format("Room ID: %d | Name: %s | Type: %s | Tickets Booked: %d",
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getString("room_type"),
                        rs.getInt("ticket_count"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving rooms with ticket count: " + e.getMessage());
        }
        return list;
    }

    // VIEW VACANT SEATS FOR A SPECIFIC SCREENING
    public ArrayList<String> getVacantSeatsForScreening(int screeningId) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT s.seat_id, s.seat_no, s.room_id " +
                    "FROM Seats s " +
                    "WHERE s.room_id = (SELECT room_id FROM Screenings WHERE screening_id = ?) " +
                    "AND s.seat_id NOT IN ( " +
                    "    SELECT seat_id FROM TicketBookings WHERE screening_id = ? " +
                    ")";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, screeningId);
            pst.setInt(2, screeningId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String info = String.format("Seat ID: %d | Seat No: %s | Room ID: %d",
                        rs.getInt("seat_id"),
                        rs.getString("seat_no"),
                        rs.getInt("room_id"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving vacant seats: " + e.getMessage());
        }
        return list;
    }

    // VIEW ROOMS WITH MOVIE SCREENING INFORMATION
    public ArrayList<String> getRoomsWithMovieInfo() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_name, m.movie_name AS movie_name, s.screening_date, s.screening_start_time " +
                    "FROM Rooms r " +
                    "JOIN Screenings s ON r.room_id = s.room_id " +
                    "JOIN Movies m ON s.movie_id = m.movie_id " +
                    "ORDER BY s.screening_date, s.screening_start_time";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String info = String.format("Room: %s | Movie: %s | Date: %s | Time: %s",
                        rs.getString("room_name"),
                        rs.getString("movie_name"),
                        rs.getDate("screening_date"),
                        rs.getTime("screening_start_time"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving rooms with movie info: " + e.getMessage());
        }
        return list;
    }

    // VIEW MOVIE GENRE AND AGE RATING FOR SCREENINGS
    public ArrayList<String> getMovieDetailsForScreenings() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT r.room_name, m.movie_name, m.genre, m.age_rating, s.screening_date, s.screening_start_time " +
                    "FROM Rooms r " +
                    "JOIN Screenings s ON r.room_id = s.room_id " +
                    "JOIN Movies m ON s.movie_id = m.movie_id " +
                    "ORDER BY s.screening_date, s.screening_start_time";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String info = String.format("Room: %s | Movie: %s | Genre: %s | Rating: %s | Date: %s | Time: %s",
                        rs.getString("room_name"),
                        rs.getString("movie_name"),
                        rs.getString("genre"),
                        rs.getString("age_rating"),
                        rs.getDate("screening_date"),
                        rs.getTime("screening_start_time"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving movie details: " + e.getMessage());
        }
        return list;
    }

    // VIEW REVENUE GENERATED USING SCREENING PRICE
    public ArrayList<String> getRevenueByRoom() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_name, " +
                    "SUM(s.price) AS total_revenue, " +
                    "COUNT(tb.ticket_no) AS tickets_sold " +
                    "FROM Rooms r " +
                    "JOIN Screenings s ON r.room_id = s.room_id " +
                    "JOIN TicketBookings tb ON s.screening_id = tb.screening_id " +
                    "WHERE tb.ticket_status != 'Canceled' AND tb.ticket_status != 'Refunded' " +
                    "GROUP BY r.room_id, r.room_name " +
                    "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String info = String.format("Room: %s | Tickets Sold: %d | Total Revenue: PHP %.2f",
                        rs.getString("room_name"),
                        rs.getInt("tickets_sold"),
                        rs.getDouble("total_revenue"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving revenue: " + e.getMessage());
        }
        return list;
    }

    // VIEW VENUE CAPACITY VS ACTUAL ATTENDANCE
    public ArrayList<String> getVenueCapacityVsAttendance() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT v.venue_id, v.venue_name, " +
                    "COUNT(DISTINCT s.seat_id) AS total_capacity, " +
                    "COUNT(DISTINCT tb.ticket_no) AS total_attendance " +
                    "FROM Venues v " +
                    "JOIN Rooms r ON v.venue_id = r.venue_id " +
                    "LEFT JOIN Seats s ON r.room_id = s.room_id " +
                    "LEFT JOIN Screenings sc ON r.room_id = sc.room_id " +
                    "LEFT JOIN TicketBookings tb ON sc.screening_id = tb.screening_id " +
                    "WHERE tb.ticket_status != 'Canceled' OR tb.ticket_status IS NULL " +
                    "GROUP BY v.venue_id, v.venue_name";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int capacity = rs.getInt("total_capacity");
                int attendance = rs.getInt("total_attendance");
                double percentage = capacity > 0 ? (attendance * 100.0 / capacity) : 0;
                
                String info = String.format("Venue: %s | Capacity: %d | Attendance: %d | Occupancy: %.2f%%",
                        rs.getString("venue_name"),
                        capacity,
                        attendance,
                        percentage);
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving capacity vs attendance: " + e.getMessage());
        }
        return list;
    }

    // VIEW TOTAL SCREENINGS ACROSS ALL VENUES
    public ArrayList<String> getTotalScreeningsByVenue() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT v.venue_id, v.venue_name, COUNT(s.screening_id) AS total_screenings " +
                    "FROM Venues v " +
                    "JOIN Rooms r ON v.venue_id = r.venue_id " +
                    "JOIN Screenings s ON r.room_id = s.room_id " +
                    "GROUP BY v.venue_id, v.venue_name " +
                    "ORDER BY total_screenings DESC";

        try (Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String info = String.format("Venue: %s | Total Screenings: %d",
                        rs.getString("venue_name"),
                        rs.getInt("total_screenings"));
                list.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving total screenings: " + e.getMessage());
        }
        return list;
    }
}