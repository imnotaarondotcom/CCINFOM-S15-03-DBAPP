public class Room {
    private final int ROOM_ID;
    private final String ROOM_NAME;
    private final String ROOM_TYPE;
    private final int VENUE_ID;

    public Room(int roomId, String roomName, String roomType, int venueId) {
        this.ROOM_ID = roomId;
        this.ROOM_NAME = roomName;
        this.ROOM_TYPE = roomType;
        this.VENUE_ID = venueId;
    }

    @Override
    public String toString()
    {
        return "[ID: " + ROOM_ID + "] " + ROOM_NAME + " | " + ROOM_TYPE + " | Venue ID: " + VENUE_ID;
    }

    public int getRoomId() { return ROOM_ID; }
    public String getRoomName() { return ROOM_NAME; }
    public String getRoomType() { return ROOM_TYPE; }
    public int getVenueId() { return VENUE_ID; }
}
