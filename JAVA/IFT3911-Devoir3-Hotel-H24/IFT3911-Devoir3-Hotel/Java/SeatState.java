interface SeatState {
    void book(Seat seat, Integer UserId);
    void unbook(Seat seat);
    void pay(Seat seat);
}

class AvailableState implements SeatState {
    public void book(Seat seat, Integer UserId) {
        seat.setState(new BookedState());
        seat.setUser(UserId);
    }

    public void unbook(Seat seat) {
    }

    public void pay(Seat seat) {
    }
}

class BookedState implements SeatState {
    public void book(Seat seat, Integer UserId) {
    }

    public void unbook(Seat seat) {
        seat.setState(new AvailableState());
    }

    public void pay(Seat seat) {
        seat.setState(new PaidState());
    }
}

class PaidState implements SeatState {
    public void book(Seat seat, Integer UserId) {
    }

    public void unbook(Seat seat) {
    }

    public void pay(Seat seat) {
    }
}
