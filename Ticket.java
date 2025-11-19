public class Ticket {

    private int ticketNo;
    private int screeningId;
    private int seatId;
    private String ticketStatus;

    // Constructor
    public Ticket(int ticketNo, int screeningId, int seatId, String ticketStatus) {
        this.ticketNo = ticketNo;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.ticketStatus = ticketStatus;
    }

    // Getters and Setters
    public int getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(int ticketNo) {
        this.ticketNo = ticketNo;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketNo=" + ticketNo +
                ", screeningId=" + screeningId +
                ", seatId=" + seatId +
                ", ticketStatus='" + ticketStatus + '\'' +
                '}';
    }
}
