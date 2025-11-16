public class Movie 
{
    private static int nextId = 1;

    private int movieId;
    private String movieName;
    private String genre;
    private String ageRating;
    private int duration;

    public Movie(String movieName, String genre, String ageRating, int duration)
    {
        this.movieId = nextId++;
        this.movieName = movieName;
        this.genre = genre;
        this.ageRating = ageRating;
        this.duration = duration;
        this.movieId = nextId++;
        this.movieName = movieName;
        this.genre = genre;
        this.ageRating = ageRating;
        this.duration = duration;
    }

    public int getMovieId(){ return movieId; }
    public String getMovieName(){ return movieName; }
    public String getGenre(){ return genre; }
    public String getAgeRating(){ return ageRating; }
    public int getDuration(){ return duration; }

    public void setMovieId(int movieId){ this.movieId = movieId; }
    public void setMovieName(String movieName){ this.movieName = movieName; }
    public void setGenre(String genre){ this.genre = genre; }
    public void setAgeRating(String ageRating){ this.ageRating = ageRating; }
    public void setDuration(int duration){ this.duration = duration; }

    public void decrementNextId(){ nextId--; }

    @Override
    public String toString() { return "[ID: " + movieId + "] " + movieName + " (" + genre + ", " + ageRating + ", " + duration + " mins)"; }
}