import java.sql.*;
import java.util.ArrayList;

public class MovieDao 
{

    // ADD MOVIE
    public boolean addMovie(Movie movie) 
    {
        String command = "INSERT INTO Movies (movie_name, genre, age_rating, duration) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)) 
        {

            statement.setString(1, movie.getMovieName());
            statement.setString(2, movie.getGenre());
            statement.setString(3, movie.getAgeRating());
            statement.setInt(4, movie.getDuration());

            int updated = statement.executeUpdate();

            if (updated > 0) 
            {
                try (ResultSet keys = statement.getGeneratedKeys()) 
                {
                    if (keys.next()) 
                    {
                        movie.setMovieId(keys.getInt(1));
                    }
                }
                return true;
            }

        } 
        catch (SQLException e) 
        {
            System.out.println("Error adding movie: " + e.getMessage());
        }
        return false;
    }


    // GET ALL MOVIES
    public ArrayList<Movie> getAllMovies() 
    {
        ArrayList<Movie> movies = new ArrayList<>();
        String command = "SELECT * FROM Movies ORDER BY movie_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command);
             ResultSet rs = statement.executeQuery()) 
        {

            while (rs.next()) 
            {
                int id = rs.getInt("movie_id");
                String name = rs.getString("movie_name");
                String genre = rs.getString("genre");
                String ageRating = rs.getString("age_rating");
                int duration = rs.getInt("duration");

                Movie movie = new Movie(id, name, genre, ageRating, duration);
                movies.add(movie);
            }

        } 
        catch (SQLException e) 
        {
            System.out.println("Error getting movies: " + e.getMessage());
        }
        return movies;
    }


    // GET MOVIE BY ID
    public Movie getMovieById(int movieId) 
    {
        String command = "SELECT * FROM Movies WHERE movie_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) 
        {

            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) 
            {
                int id = rs.getInt("movie_id");
                String name = rs.getString("movie_name");
                String genre = rs.getString("genre");
                String ageRating = rs.getString("age_rating");
                int duration = rs.getInt("duration");

                return new Movie(id, name, genre, ageRating, duration);
            }

        } 
        catch (SQLException e) 
        {
            System.out.println("Error getting movie: " + e.getMessage());
        }
        return null;
    }


    // UPDATE MOVIE
    public boolean updateMovie(Movie movie) 
    {
        String command = "UPDATE Movies SET movie_name = ?, genre = ?, age_rating = ?, duration = ? WHERE movie_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) 
        {

            statement.setString(1, movie.getMovieName());
            statement.setString(2, movie.getGenre());
            statement.setString(3, movie.getAgeRating());
            statement.setInt(4, movie.getDuration());
            statement.setInt(5, movie.getMovieId());

            int updated = statement.executeUpdate();
            return updated > 0;

        } 
        catch (SQLException e) 
        {
            System.out.println("Error updating movie: " + e.getMessage());
        }
        return false;
    }


    // DELETE MOVIE
    public boolean deleteMovie(int movieId) 
    {
        String command = "DELETE FROM Movies WHERE movie_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) 
        {

            statement.setInt(1, movieId);
            int updated = statement.executeUpdate();

            return updated > 0;

        } 
        catch (SQLException e) 
        {
            System.out.println("Error deleting movie: " + e.getMessage());
        }
        return false;
    }


    // SEARCH MOVIES BY NAME
    public ArrayList<Movie> searchMovieName(String keyword) 
    {
        ArrayList<Movie> movies = new ArrayList<>();
        String command = "SELECT * FROM Movies WHERE movie_name LIKE ? ORDER BY movie_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) 
        {

            statement.setString(1, "%" + keyword + "%");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) 
            {
                int id = rs.getInt("movie_id");
                String name = rs.getString("movie_name");
                String genre = rs.getString("genre");
                String ageRating = rs.getString("age_rating");
                int duration = rs.getInt("duration");

                movies.add(new Movie(id, name, genre, ageRating, duration));
            }

        } 
        catch (SQLException e) 
        {
            System.out.println("Error searching movies: " + e.getMessage());
        }

        return movies;
    }

}
