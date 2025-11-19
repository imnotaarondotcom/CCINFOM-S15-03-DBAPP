import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;


public class ScreeningsManagement {

    private Scanner scanner;
    private ScreeningDao screeningDao;
    private MovieDao movieDao;
    private VenuesDao venuesDao;
    private String accountType;
    private MovieManagement movieManagement;
    private VenueManagement venueManagement;

    public ScreeningsManagement(Scanner scanner, String accountType, MovieManagement movieManagement, VenueManagement venueManagement) {
        this.scanner = scanner;
        this.accountType = accountType;
        this.movieManagement = movieManagement;
        this.venueManagement = venueManagement;
        this.screeningDao = new ScreeningDao();
        this.movieDao = new MovieDao();
        this.venuesDao = new VenuesDao();
    }

    // ------------------------- MAIN MENU -------------------------
    public void openMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Screenings Menu ===");

            if (accountType.equals("Admin")) {
                System.out.println("1. View all screenings");
                System.out.println("2. Add new screening");
                System.out.println("3. Edit screening");
                System.out.println("4. Cancel screening");
                System.out.println("5. Back");
            } else {
                System.out.println("1. View all screenings");
                System.out.println("2. Back");
            }

            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (accountType.equals("Admin")) {
                switch (choice) {
                    case 1 -> viewAllScreenings();
                    case 2 -> addScreening();
                    case 3 -> editScreening();
                    case 4 -> cancelScreening();
                    case 5 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                switch (choice) {
                    case 1 -> viewAllScreenings();
                    case 2 -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    // ------------------------- VIEW SCREENINGS -------------------------
    public void viewAllScreenings() {
        String sql = """
            SELECT s.screening_id, v.venue_name, r.room_name, m.movie_name,
                   m.genre, m.age_rating, s.screening_date,
                   s.screening_start_time, s.screening_end_time, s.price, s.screening_status
            FROM Screenings s
            JOIN Venues v ON s.venue_id = v.venue_id
            JOIN Rooms r ON s.room_id = r.room_id
            JOIN Movies m ON s.movie_id = m.movie_id
            ORDER BY s.screening_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            System.out.println("\n=== All Screenings ===");

            while (rs.next()) {
                System.out.printf("""
                    
                    Screening ID: %d
                    Venue: %s
                    Room: %s
                    Movie: %s
                    Genre: %s
                    Rating: %s
                    Date: %s
                    Start: %s | End: %s
                    Price: %.2f
                    Status: %s
                    ----------------------------------------
                    """,
                        rs.getInt("screening_id"),
                        rs.getString("venue_name"),
                        rs.getString("room_name"),
                        rs.getString("movie_name"),
                        rs.getString("genre"),
                        rs.getString("age_rating"),
                        rs.getDate("screening_date"),
                        rs.getTime("screening_start_time"),
                        rs.getTime("screening_end_time"),
                        rs.getDouble("price"),
                        rs.getString("screening_status")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error viewing screenings: " + e.getMessage());
        }
    }


    // ------------------------- ADD SCREENING -------------------------
    private void addScreening() 
    {
        System.out.println("\n[ADD SCREENING]");

        // Select Movie
        ArrayList<Movie> movies = movieDao.getAllMovies();
        if (movies.isEmpty()) 
        {
            System.out.println("No movies available.");
            return;
        }

        movieManagement.viewMovies();
        System.out.println("Select a movie:");
        int movieId = scanner.nextInt();
        scanner.nextLine();
        Movie movie = movieDao.getMovieById(movieId);

        if (movie == null) 
        {
            System.out.println("Invalid movie selection.");
            return;
        }

        // Select Venue
        ArrayList<Venues> venues = venueManagement.getAllVenues();
        if (venues.isEmpty()) 
        {
            System.out.println("No venues available.");
            return;
        }
        venueManagement.viewVenues(); 
        System.out.println("Select a venue:");
        int venueId = scanner.nextInt();
        scanner.nextLine();
        Venues venue = venueManagement.getVenueById(venueId);

        if (venue == null)
        {
            System.out.println("Invalid venue selection.");
            return;
        }

        // Select Room
        ArrayList<Room> rooms = venuesDao.getRoomsByVenue(venueId);
        if (rooms.isEmpty()) 
        {
            System.out.println("No rooms available at this venue.");
            return;
        }

        venueManagement.viewRoomsAtVenue(venueId);
        int roomId = getValidRoomId(rooms);

        // Enter Date & Start Time
        System.out.print("Enter screening date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        System.out.print("Enter start time (HH:MM 24h): ");
        String startTimeInput = scanner.nextLine();

        LocalDate date = LocalDate.parse(dateInput);
        LocalTime startTime = LocalTime.parse(startTimeInput);

        // Calculate end time based on movie duration
        LocalTime endTime = startTime.plusMinutes(movie.getDuration());

        // Enter price for the screening
        System.out.print("Enter ticket price for this screening: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        if (!screeningDao.isRoomAvailable(roomId, date, startTime, endTime, null)) {
            System.out.println("Selected room is already booked at this time. Choose another room or time.");
            return;
        }

        // Add screening
        boolean added = screeningDao.addScreening(movieId, venueId, roomId, price, date, startTime, endTime);

        if (added)
            System.out.println("Screening added successfully!");
        else
            System.out.println("Failed to add screening. Time slot may be occupied.");
    
    }

    // ------------------------- EDIT SCREENING -------------------------
    private void editScreening() {
        viewAllScreenings();

        System.out.print("Enter Screening ID to edit: ");
        int screeningId = Integer.parseInt(scanner.nextLine());

        Screening s = screeningDao.getScreeningById(screeningId);
        if (s == null) {
            System.out.println("Screening not found.");
            return;
        }

        System.out.println("""
            What do you want to edit?
            1. Movie
            2. Schedule
            3. Price
            4. Room
        """);

        System.out.print("Choice: ");
        int choice = Integer.parseInt(scanner.nextLine());

        try (Connection conn = DBConnection.getConnection()) {

            switch (choice) {
                case 1 -> {
                    movieManagement.viewMovies();
                    System.out.print("Enter new Movie ID: ");
                    int movieId = Integer.parseInt(scanner.nextLine());
                    Movie movie = movieDao.getMovieById(movieId);
                    if (movie == null) {
                        System.out.println("Movie not found.");
                        return;
                    }
                    updateField(conn, "movie_id", movieId, screeningId);
                    s.setMovieId(movieId);
                }

                case 2 -> {
                    Movie movie = movieDao.getMovieById(s.getMovieId());
                    if (movie == null) {
                        System.out.println("Movie not found for this screening.");
                        return;
                    }
                    System.out.print("New Date (YYYY-MM-DD): ");
                    String dateInput = scanner.nextLine();

                    System.out.print("Enter start time (HH:MM 24h): ");
                    String startTimeInput = scanner.nextLine();

                    LocalDate date = LocalDate.parse(dateInput);
                    LocalTime startTime = LocalTime.parse(startTimeInput);

                    // Calculate end time based on movie duration
                    LocalTime endTime = startTime.plusMinutes(movie.getDuration());

                    updateField(conn, "screening_date", date, screeningId);
                    updateField(conn, "screening_start_time", startTime, screeningId);
                    updateField(conn, "screening_end_time", endTime, screeningId);

                    s.setScreeningDate(date);
                    s.setScreeningStartTime(startTime);
                    s.setScreeningEndTime(endTime);

                    System.out.println("Schedule updated successfully!");
                }

                case 3 -> {
                    System.out.print("Enter new price: ");
                    double price = Double.parseDouble(scanner.nextLine());

                    updateField(conn, "price", price, screeningId);
                    s.setPrice(price);
                }

                case 4 -> {
                    ArrayList<Room> rooms = venuesDao.getRoomsByVenue(s.getVenueId());
                    venueManagement.viewRoomsAtVenue(s.getVenueId());
                    int roomId = getValidRoomId(rooms);

                    // Check new room availability with same schedule
                    if (!screeningDao.isRoomAvailable(roomId, s.getScreeningDate(), s.getScreeningStartTime(), s.getScreeningEndTime(), s.getScreeningId())) {
                        System.out.println("Selected room is already booked at this time. Choose another room.");
                        return;
                    }

                    updateField(conn, "room_id", roomId, screeningId);
                    s.setRoomId(roomId);
                }

                default -> System.out.println("Invalid choice.");
            }

        } catch (SQLException e) {
            System.out.println("Error editing screening: " + e.getMessage());
        }
    }

    private int getValidRoomId(ArrayList<Room> rooms) {
        int roomId;

        while (true) {
            System.out.print("Enter Room ID: ");

            try {
                roomId = Integer.parseInt(scanner.nextLine());

                boolean exists = false;
                for (Room r : rooms) {
                    if (r.getRoomId() == roomId) {
                        exists = true;
                        break;
                    }
                }

                if (exists) return roomId;

                System.out.println("Room ID not found. Try again.");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }



    private void updateField(Connection conn, String field, Object value, int id) throws SQLException {
        String sql = "UPDATE Screenings SET " + field + " = ? WHERE screening_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setObject(1, value);
            pst.setInt(2, id);
            pst.executeUpdate();
        }
        System.out.println("Updated " + field + " successfully.");
    }

    // ------------------------- CANCEL SCREENING -------------------------
    private void cancelScreening() {
        viewAllScreenings();

        System.out.print("Enter Screening ID to cancel: ");
        int id = Integer.parseInt(scanner.nextLine());

        screeningDao.updateScreeningStatus(id, "Cancelled");
        System.out.println("Screening cancelled successfully!");
    }
}
