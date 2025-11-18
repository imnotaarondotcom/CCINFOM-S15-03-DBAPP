import java.sql.*;
import java.util.ArrayList;


public class VenuesDao{

    //This method allows us to add venues into the database
    public boolean addVenue(Venues venue) {
        String command = "INSERT INTO Venues (venue_name, address) VALUES (?, ?)";
        
        try (Connection connect = DBConnection.getConnection();
             PreparedStatement statement = connect.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, venue.getVenue_name());
            statement.setString(2, venue.getAddress());
            
            int updated = statement.executeUpdate();
            
            if (updated > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        venue.setVenue_id(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } 
        catch (SQLException error) {
            System.out.println("Error adding venue: " + error.getMessage());
        }
        return false;
    }
    
    //This method gets all the venues (use this for printing)
    public ArrayList<Venues> getAllVenues() {
        ArrayList<Venues> venues = new ArrayList<>();
        String command = "SELECT * FROM Venues ORDER BY venue_id";
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command);
             ResultSet rs = statement.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("venue_id");
                String name = rs.getString("venue_name");
                String address = rs.getString("address");
                
                Venues venue = new Venues(id, name, address);
                venues.add(venue);
            }
        } catch (SQLException error) {
            System.out.println("Error getting venues: " + error.getMessage());
        }
        return venues;
    }
    
    //This function is used to get a venue by their venue ID
    public Venues getVenueById(int venueId) {
        String command = "SELECT * FROM Venues WHERE venue_id = ?";
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {
            
            statement.setInt(1, venueId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("venue_id");
                String name = rs.getString("venue_name");
                String address = rs.getString("address");
                
                return new Venues(id, name, address);
            }
        } catch (SQLException error) {
            System.out.println("Error getting venue: " + error.getMessage());
        }
        return null;
    }
    
    //Allows us to change a venues name and address
    public boolean updateVenue(Venues venue) {
        String command = "UPDATE Venues SET venue_name = ?, address = ? WHERE venue_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) {
            
            statement.setString(1, venue.getVenue_name());
            statement.setString(2, venue.getAddress());
            statement.setInt(3, venue.getVenue_id());
            
            int updated = statement.executeUpdate();
            return updated > 0;
            
        } catch (SQLException error) {
            System.out.println("Error updating venue: " + error.getMessage());
        }
        return false;
    }
    
    // Delete venue
    public boolean deleteVenue(int venueId) {
        String command = "DELETE FROM Venues WHERE venue_id = ?";
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {
            
            statement.setInt(1, venueId);
            int updated = statement.executeUpdate();
            return updated > 0;
            
        } catch (SQLException error) {
            System.out.println("Error deleting venue: " + error.getMessage());
        }
        return false;
    }
    
    // Additional method: Search venues by name
    public ArrayList<Venues> searchVenueName(String namePattern) {
        ArrayList<Venues> venues = new ArrayList<>();
        String command = "SELECT * FROM Venues WHERE venue_name LIKE ? ORDER BY venue_name";
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {
            
            statement.setString(1, "%" + namePattern + "%");
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("venue_id");
                String name = rs.getString("venue_name");
                String address = rs.getString("address");
                
                Venues venue = new Venues(id, name, address);
                venues.add(venue);
            }
        } catch (SQLException error) {
            System.out.println("Error searching venues: " + error.getMessage());
        }
        return venues;
    }

    public ArrayList<String> getMoviesByVenue(int venueId) {
    ArrayList<String> movies = new ArrayList<>();
    String command = """
        SELECT DISTINCT m.movie_name, s.screening_date, s.screening_start_time 
        FROM Movies m 
        JOIN Screenings s ON m.movie_id = s.movie_id 
        JOIN Rooms r ON s.room_id = r.room_id 
        WHERE r.venue_id = ? 
        ORDER BY s.screening_date, s.screening_start_time
        """;
    
     try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(command)) {
        
        statement.setInt(1, venueId);
        ResultSet rs = statement.executeQuery();
        
        while (rs.next()) {
            String movieInfo = String.format("Movie: %s Date: %s Time: %s",
                rs.getString("movie_name"),
                rs.getDate("screening_date"),
                rs.getTime("screening_start_time"));
            movies.add(movieInfo);
        }
    } catch (SQLException error) {
        System.out.println("Error getting movies for venue: " + error.getMessage());
    }
    return movies;
}

public ArrayList<Room> getRoomsByVenue(int venueId) {
    ArrayList<Room> rooms = new ArrayList<>();
    String sql = "SELECT room_id, room_name, room_type, venue_id FROM Rooms WHERE venue_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, venueId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            rooms.add(new Room(
                rs.getInt("room_id"),
                rs.getString("room_name"),
                rs.getString("room_type"),
                rs.getInt("venue_id")
            ));
        }

    } catch (SQLException e) {
        System.out.println("Error fetching rooms: " + e.getMessage());
    }

    return rooms;
}


public ArrayList<String> getScreeningsByVenue(int venueId) {
    ArrayList<String> screenings = new ArrayList<>();
    String command = """
        SELECT m.movie_name, r.room_name, s.screening_date, s.screening_start_time, s.screening_end_time 
        FROM Screenings s 
        JOIN Movies m ON s.movie_id = m.movie_id 
        JOIN Rooms r ON s.room_id = r.room_id 
        WHERE r.venue_id = ? 
        ORDER BY s.screening_date, s.screening_start_time
        """;
    
    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(command)) {
        
        statement.setInt(1, venueId);
        ResultSet rs = statement.executeQuery();
        
        while (rs.next()) {
            String screeningInfo = String.format("Movie: %s Room: %s Date: %s Time: %s - %s",
                rs.getString("movie_name"),
                rs.getString("room_name"),
                rs.getDate("screening_date"),
                rs.getTime("screening_start_time"),
                rs.getTime("screening_end_time"));
            screenings.add(screeningInfo);
        }
    } catch (SQLException error) {
        System.out.println("Error getting screenings for venue: " + error.getMessage());
    }
    return screenings;
}

public int getTicketsSoldByVenue(int venueId) {
    String command = """
        SELECT COUNT(tb.ticket_no) as ticket_count 
        FROM TicketBookings tb 
        JOIN Screenings s ON tb.screening_id = s.screening_id 
        JOIN Rooms r ON s.room_id = r.room_id 
        WHERE r.venue_id = ? AND tb.ticket_status = 'Booked'
        """;
    
    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(command)) {
        
        statement.setInt(1, venueId);
        ResultSet rs = statement.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("ticket_count");
        }
    } catch (SQLException error) {
        System.out.println("Error getting tickets count: " + error.getMessage());
    }
    return 0;
}

}
