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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }


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
