package nimons.exceptions;

public class StationFullException extends GameException {
    
    private String stationName;
    private int capacity;
    
    public StationFullException(String stationName, int capacity) {
        super(String.format("%s is full (capacity: %d)", stationName, capacity));
        this.stationName = stationName;
        this.capacity = capacity;
    }
    
    public String getStationName() {
        return stationName;
    }
    
    public int getCapacity() {
        return capacity;
    }
}
