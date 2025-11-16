import java.util.ArrayList;
import java.util.Scanner;

public class MovieMenu
{
    public static void main(String[] args)
    {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        Scanner sc = new Scanner(System.in);
        boolean exit = false;

        System.out.println("\n-- Movie Records --");

        while (!exit)
        {
            System.out.println("\nSelect an action:");
            System.out.println("[1] Add new movie");
            System.out.println("[2] Edit movie");
            System.out.println("[3] Delete movie");
            System.out.println("[4] List movies");
            System.out.println("[0] Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume leftover newline

            switch (choice)
            {
                case 1:
                    addRecord(movies, sc);
                    break;
                case 2:
                    editMovie(movies, sc);
                    break;
                case 3:
                    deleteMovie(movies, sc);
                    break;
                case 4:
                    listMovies(movies);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        }

        sc.close();
    }

    private static void addRecord(ArrayList<Movie> movies, Scanner sc)
    {
        System.out.println("\n[ADD NEW MOVIE]");

        System.out.print("Movie name: ");
        String movieName = sc.nextLine();

        System.out.print("Genre: ");
        String genre = sc.nextLine();

        int ratingChoice = 0;
        String ageRating = "";

        while (ratingChoice < 1 || ratingChoice > 4)
        {
            System.out.println("Select age rating: ");
            System.out.println("[1] G");
            System.out.println("[2] PG");
            System.out.println("[3] PG-13");
            System.out.println("[4] R");
            System.out.print("Enter: ");

            ratingChoice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (ratingChoice)
            {
                case 1: ageRating = "G"; break;
                case 2: ageRating = "PG"; break;
                case 3: ageRating = "PG-13"; break;
                case 4: ageRating = "R"; break;
                default:
                    System.out.println("Invalid selection.");
            }
        }

        System.out.print("Duration (in minutes): ");
        int duration = sc.nextInt();
        sc.nextLine(); // consume newline

        Movie newMovie = new Movie(movieName, genre, ageRating, duration);
        movies.add(newMovie);

        System.out.println("Movie added successfully!");
        System.out.println(newMovie);
    }

    private static void listMovies(ArrayList<Movie> movies)
    {
        System.out.println("\n--- MOVIE LIST ---");
        if (movies.isEmpty())
        {
            System.out.println("No movies found.");
            return;
        }

        for (Movie m : movies)
        {
            System.out.println(m);
        }
    }

    private static Movie findMovieById(ArrayList<Movie> movies, int id)
    {
        for (Movie m : movies)
        {
            if (m.getMovieId() == id)
                return m;
        }
        return null; // not found
    }

    private static void editMovie(ArrayList<Movie> movies, Scanner sc)
    {
        System.out.println("\n[EDIT MOVIE]");
        if (movies.isEmpty())
        {
            System.out.println("No movies to edit.");
            return;
        }

        listMovies(movies);

        System.out.print("Enter the Movie ID to edit: ");
        int id = sc.nextInt();
        sc.nextLine(); // consume newline

        Movie movie = findMovieById(movies, id);
        if (movie == null)
        {
            System.out.println("Movie with ID " + id + " not found.");
            return;
        }

        System.out.print("New name (leave blank to keep '" + movie.getMovieName() + "'): ");
        String name = sc.nextLine();
        if (!name.isEmpty()) movie.setMovieName(name);

        System.out.print("New genre (leave blank to keep '" + movie.getGenre() + "'): ");
        String genre = sc.nextLine();
        if (!genre.isEmpty()) movie.setGenre(genre);

        int ratingChoice = 0;
        while (true)
        {
            System.out.println("Select new age rating (leave 0 to keep '" + movie.getAgeRating() + "'):");
            System.out.println("[1] G");
            System.out.println("[2] PG");
            System.out.println("[3] PG-13");
            System.out.println("[4] R");
            System.out.print("Enter: ");

            ratingChoice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (ratingChoice == 0) break;

            switch (ratingChoice)
            {
                case 1: movie.setAgeRating("G"); break;
                case 2: movie.setAgeRating("PG"); break;
                case 3: movie.setAgeRating("PG-13"); break;
                case 4: movie.setAgeRating("R"); break;
                default:
                    System.out.println("Invalid selection.");
                    continue;
            }
            break;
        }

        System.out.print("New duration in minutes (enter 0 to keep '" + movie.getDuration() + "'): ");
        int duration = sc.nextInt();
        sc.nextLine();
        if (duration > 0) movie.setDuration(duration);

        System.out.println("Movie updated successfully!");
        System.out.println(movie);
    }

    private static void deleteMovie(ArrayList<Movie> movies, Scanner sc)
    {
        System.out.println("\n[DELETE MOVIE]");
        if (movies.isEmpty())
        {
            System.out.println("No movies to delete.");
            return;
        }

        listMovies(movies);

        System.out.print("Enter the Movie ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine(); // consume newline

        Movie movie = findMovieById(movies, id);
        if (movie == null)
        {
            System.out.println("Movie with ID " + id + " not found.");
            return;
        }

        movie.decrementNextId();
        movies.remove(movie);
        System.out.println("Movie '" + movie.getMovieName() + "' deleted successfully!");
    }
}
