public class Seat {
    private final int SEAT_ID;
    private final String SEAT_NO;
    private final int ROOM_ID;

    public Seat(int seatId, String seatNo, int roomId) {
        this.SEAT_ID = seatId;
        this.SEAT_NO = seatNo;
        this.ROOM_ID = roomId;
    }

    public int getSeatId() { return SEAT_ID; }
    public String getSeatNo() { return SEAT_NO; }
    public int getRoomId() { return ROOM_ID; }
}
