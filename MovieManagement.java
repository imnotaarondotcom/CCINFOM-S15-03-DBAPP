import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;
import java.sql.Time;


public class MovieManagement 
{
    private MovieDao movieDao;
    private VenueManagement venueManagement;
    private Scanner scanner;

    public MovieManagement(MovieDao movieDao, VenueManagement venueManagement, Scanner scanner) 
    {
        this.movieDao = movieDao;
        this.venueManagement = venueManagement;
        this.scanner = scanner;
    }

    public void open() 
    {
        boolean running = true;

        while (running) 
        {

            System.out.println("\n=== MOVIE MANAGEMENT ===");
            System.out.println("1. View movies");
            System.out.println("2. Add movie");
            System.out.println("3. Edit movie");
            System.out.println("4. Delete movie");
            System.out.println("5. Search movie");
            System.out.println("6. Assign movie to screening");
            System.out.println("7. Back");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) 
            {
                case 1 -> viewMovies();
                case 2 -> addMovie();
                case 3 -> editMovie();
                case 4 -> deleteMovie();
                case 5 -> searchMovie();
                case 6 -> assignMovie();
                case 7 -> running = false;
                default -> System.out.println("Invalid input, try again.");
            }
        }
    }

    // VIEW MOVIES
    private void viewMovies() 
    {
        ArrayList<Movie> movies = movieDao.getAllMovies();
        System.out.println("\n--- MOVIE LIST ---");

        if (movies.isEmpty()) 
        {
            System.out.println("No movies found.");
            return;
        }

        for (Movie m : movies) 
        {
            System.out.println(m.toString());
        }
    }

    // ADD MOVIE
    private void addMovie() 
    {
        System.out.println("\n[ADD MOVIE]");

        System.out.print("Movie name: ");
        String name = scanner.nextLine();

        System.out.print("Genre: ");
        String genre = scanner.nextLine();

        String rating = getAgeRatingInput();

        System.out.print("Duration (minutes): ");
        int duration = scanner.nextInt();
        scanner.nextLine();

        Movie movie = new Movie(0, name, genre, rating, duration);
        boolean added = movieDao.addMovie(movie);

        if (added)
            System.out.println("Movie added!");
        else
            System.out.println("Error adding movie.");
    }

    private String getAgeRatingInput() 
    {
        while (true) 
        {
            System.out.println("Select age rating:");
            System.out.println("[1] G");
            System.out.println("[2] PG");
            System.out.println("[3] PG-13");
            System.out.println("[4] R");
            System.out.print("Enter: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            return switch (choice) 
            {
                case 1 -> "G";
                case 2 -> "PG";
                case 3 -> "PG-13";
                case 4 -> "R";
                default -> {
                    System.out.println("Invalid rating. Try again.");
                    yield null;
                }
            };
        }
    }

    // DELETE MOVIE
    private void deleteMovie() 
    {
        System.out.println("\n[DELETE MOVIE]");
        viewMovies();
        System.out.print("\nEnter movie ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        boolean deleted = movieDao.deleteMovie(id);

        if (deleted)
            System.out.println("Movie deleted.");
        else
            System.out.println("Failed to delete movie.");
    }

    // SEARCH MOVIE BY NAME
    private void searchMovie() 
    {
        System.out.println("\n[SEARCH MOVIE]");
        System.out.print("Search movie name: ");
        String name = scanner.nextLine();

        ArrayList<Movie> results = movieDao.searchMovieName(name);

        System.out.println("\n== RESULTS ==");
        if (results.isEmpty())
            System.out.println("No matching movies.");
        else
            results.forEach(m -> System.out.println(m.toString()));
    }

    // EDIT MOVIE
    private void editMovie() 
    {
        System.out.println("\n[EDIT MOVIE]");
        viewMovies();

        System.out.print("\nEnter movie ID to edit: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Movie movie = movieDao.getMovieById(id);
        if (movie == null) 
        {
            System.out.println("Movie not found.");
            return;
        }

        System.out.println(movie.toString());

        System.out.println("What do you want to edit?");
        System.out.println("1. Name");
        System.out.println("2. Genre");
        System.out.println("3. Age Rating");
        System.out.println("4. Duration");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) 
        {
            case 1 -> {
                System.out.print("Enter new name: ");
                movie.setMovieName(scanner.nextLine());
            }
            case 2 -> {
                System.out.print("Enter new genre: ");
                movie.setGenre(scanner.nextLine());
            }
            case 3 -> movie.setAgeRating(getAgeRatingInput());
            case 4 -> {
                System.out.print("New duration: ");
                movie.setDuration(scanner.nextInt());
                scanner.nextLine();
            }
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        boolean updated = movieDao.updateMovie(movie);

        if (updated) System.out.println("Movie updated!");
        else System.out.println("Failed to update movie.");
    }

    // ASSIGN MOVIE TO SCREENING
    private void assignMovie() 
    {
        System.out.println("\n[ASSIGN MOVIE TO SCREENING]");
        
        // Select Movie
        ArrayList<Movie> movies = movieDao.getAllMovies();
        if (movies.isEmpty()) 
        {
            System.out.println("No movies available.");
            return;
        }

        viewMovies();
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
        ArrayList<String> rooms = venueManagement.getRoomsByVenue(venueId);
        if (rooms.isEmpty()) 
        {
            System.out.println("No rooms available at this venue.");
            return;
        }
        venueManagement.viewRoomsAtVenue(venueId);
        System.out.println("Select a room:");

        int roomIndex = scanner.nextInt();
        scanner.nextLine();
        int roomId = roomIndex;

        // Enter Date & Start Time
        System.out.print("Enter screening date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        System.out.print("Enter start time (HH:MM 24h): ");
        String startTimeInput = scanner.nextLine();

        LocalDate date = LocalDate.parse(dateInput);
        LocalTime startTime = LocalTime.parse(startTimeInput);

        // Calculate end time based on movie duration
        LocalTime endTime = startTime.plusMinutes(movie.getDuration());

        // Add screening
        ScreeningDao screeningDao = new ScreeningDao();
        boolean added = screeningDao.addScreening(movieId, venueId, roomId, date, startTime, endTime);

        if (added)
            System.out.println("Screening added successfully!");
        else
            System.out.println("Failed to add screening. Time slot may be occupied.");
    }

}
