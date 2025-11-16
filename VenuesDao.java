import java.sql.*;
import java.util.ArrayList;


public class VenuesDao{

    //This method allows us to add venues into the database
    public boolean addVenue(Venues venue) {
        String command = "INSERT INTO Venues (venue_name, address) VALUES (?, ?)";
        
        try (Connection connect = ConnectToDB.getConnection();
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
        
        try (Connection connection = ConnectToDB.getConnection();
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
        
        try (Connection connection = ConnectToDB.getConnection();
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
        
        try (Connection conn = ConnectToDB.getConnection();
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
        
        try (Connection connection = ConnectToDB.getConnection();
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
        
        try (Connection connection = ConnectToDB.getConnection();
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
}