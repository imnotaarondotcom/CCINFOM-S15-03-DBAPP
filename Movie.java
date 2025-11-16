public class Movie
{
    private final int MOVIE_ID;
    private final String MOVIE_NAME;
    private final String GENRE;
    private final int AGE_RATING;

    public Movie(movieId, movieName, genre, ageRating)
    {
        MOVIE_ID = movieiD;
        MOVIE_NAME = movieName;
        GENRE = genre;
        AGE_RATING = ageRating;
    }

    public int getMovieId()
    {
        return MOVIE_ID;
    }

    public String getMovieName()
    {
        return MOVIE_NAME;
    }

    public String getGenre()
    {
        return GENRE;
    }

    public int getAgeRating()
    {
        return AGE_RATING;
    }


}