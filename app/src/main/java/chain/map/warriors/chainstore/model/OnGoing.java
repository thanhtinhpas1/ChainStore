package chain.map.warriors.chainstore.model;

public class OnGoing {
    private String destination;
    private String time;

    public OnGoing(String destination, String time) {
        this.destination = destination;
        this.time = time;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
