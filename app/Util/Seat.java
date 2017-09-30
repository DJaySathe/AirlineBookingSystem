package Util;

public class Seat {
    int tid;
    String flight;

    public Seat(int tid, String flight) {
        this.tid = tid;
        this.flight = flight;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }
}
