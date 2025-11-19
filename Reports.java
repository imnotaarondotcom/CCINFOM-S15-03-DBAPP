import java.time.LocalDate;
import java.util.*;

public class Reports {
    
 
    // Generate Venue Utilization Report
    public static String generateVenueUtilizationReport() {
        StringBuilder report = new StringBuilder();
        report.append("===== VENUE UTILIZATION REPORT =====\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");
        
        try {
            TicketDao ticketsDao = new TicketDao();
            VenuesDao venuesDao = new VenuesDao();
            SeatDao seatDao = new SeatDao();
            
            List<Venues> allVenues = venuesDao.getAllVenues();
            
            report.append(String.format("%-25s %-15s %-15s %s\n", 
                "Venue Name", "Total Seats", "Occupied Seats", "Occupancy %"));
            report.append("------------------------------------------------------------------------\n");
            
            int totalVenueSeats = 0;
            int totalOccupiedSeats = 0;
            
            for (Venues venue : allVenues) {
                // Get all rooms in venue
                List<Room> rooms = venuesDao.getRoomsByVenue(venue.getVenue_id());
                
                int venueSeats = 0;
                int occupiedSeats = 0;
                
                for (Room room : rooms) {
                    // Get all seats in room
                    List<Seat> seats = seatDao.getSeatsByRoom(room.getRoomId());
                    venueSeats += seats.size();
                    
                    // Count occupied seats (from tickets)
                    for (Seat seat : seats) {
                        List<Ticket> tickets = ticketsDao.getTicketsBySeatId(seat.getSeatId());
                        if (tickets.size() > 0) {
                            occupiedSeats++;
                        }
                    }
                }
                
                double occupancyPercent = venueSeats > 0 ? (double) occupiedSeats / venueSeats * 100 : 0;
                
                report.append(String.format("%-25s %-15d %-15d %.1f%%\n", 
                    venue.getVenue_name(), venueSeats, occupiedSeats, occupancyPercent));
                
                totalVenueSeats += venueSeats;
                totalOccupiedSeats += occupiedSeats;
            }
            
            report.append("------------------------------------------------------------------------\n");
            double overallOccupancy = totalVenueSeats > 0 ? (double) totalOccupiedSeats / totalVenueSeats * 100 : 0;
            report.append(String.format("%-25s %-15d %-15d %.1f%%\n", 
                "TOTAL", totalVenueSeats, totalOccupiedSeats, overallOccupancy));
            
        } catch (Exception ex) {
            report.append("Error generating report: ").append(ex.getMessage());
        }
        
        return report.toString();
    }
}
