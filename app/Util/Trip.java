package Util;

import java.util.List;

public class Trip {
    int Id;
    List<String> segments;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }

    public Trip(int id, List<String> segments) {
        Id = id;
        this.segments = segments;
    }
}
