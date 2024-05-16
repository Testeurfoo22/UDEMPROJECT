import java.util.List;

public class Seat {
    private section section;
    private Taille taille;
    private SeatState state;
    private Integer UserId;

    public Seat(section section, Taille taille) {
        this.section = section;
        this.taille = taille;
        this.state = new AvailableState();
        this.UserId = null;
    }

    public void pay() {
        state.pay(this);
    }
    public void setState(SeatState state) {
        this.state = state;
    }
    public void setUser(Integer UserId) {
        this.UserId = UserId;
    }
    public section getSection() {
        return section;
    }
    public Taille getTaille() {
        return taille;
    }
    public SeatState getState() {
        return state;
    }
    public Integer getUser() {
        return UserId;
    }

    public static String formatSeatSection(List<Seat> seatList) {
        if (seatList.isEmpty()) return "";

        Seat sampleSeat = seatList.get(0);
        section section = sampleSeat.getSection();
        Taille taille = sampleSeat.getTaille();
        int count = seatList.size();

        return section + "," + taille + "," + count;
    }
}