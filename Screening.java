import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class Screening {
    private int screeningId;
    private int movieId;
    private int venueId;
    private int roomId;
    private LocalDate screeningDate;
    private LocalTime screeningStartTime;
    private LocalTime screeningEndTime;
    private String status;
    private double price;

    public Screening(int screeningId, int movieId, int venueId, int roomId,
                     double price, LocalDate screeningDate, LocalTime screeningStartTime, 
                     LocalTime screeningEndTime, String status) {
        this.screeningId = screeningId;
        this.movieId = movieId;
        this.venueId = venueId;
        this.roomId = roomId;
        this.screeningDate = screeningDate;
        this.screeningStartTime = screeningStartTime;
        this.screeningEndTime = screeningEndTime;
        this.status = status;
    }

    public int getScreeningId() { return screeningId; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
    public int getMovieId() { return movieId; }
    public int getRoomId() { return roomId; }
    public int getVenueId() { return venueId; }
    public LocalDate getScreeningDate() { return screeningDate; }
    public LocalTime getScreeningStartTime() { return screeningStartTime; }
    public LocalTime getScreeningEndTime() { return screeningEndTime; }
    
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public void setStatus(String status) { this.status = status; }
    public void setPrice(double price) { this.price = price; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }
    public void setScreeningStartTime(LocalTime screeningStartTime) { this.screeningStartTime = screeningStartTime; }
    public void setScreeningEndTime(LocalTime screeningEndTime) { this.screeningEndTime = screeningEndTime; }


    @Override
    public String toString() {
        return "Screening{" +
                "screeningId=" + screeningId +
                ", movieId=" + movieId +
                ", venueId=" + venueId +
                ", roomId=" + roomId +
                ", screeningDate=" + screeningDate +
                ", screeningStartTime=" + screeningStartTime +
                ", screeningEndTime=" + screeningEndTime +
                ", status=" + status +
                '}';
    }
}
