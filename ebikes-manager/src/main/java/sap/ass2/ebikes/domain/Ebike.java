package sap.ass2.ebikes.domain; // Package declaration for the business logic layer.

public class Ebike  {	
    private String id; // The unique identifier for the electric bike.
    
    // Enum representing the possible states of an electric bike.
    public enum EBikeState { 
        AVAILABLE,      // The bike is available for use.
        IN_USE,        // The bike is currently being used.
        MAINTENANCE,   // The bike is under maintenance.
        DISMISSED      // The bike is no longer in use and is dismissed.
    }
    
    private EBikeState state; // The current state of the electric bike.
    private P2d loc;          // The current location of the bike represented as a 2D point.
    private V2d direction;    // The current direction the bike is facing represented as a 2D vector.
    private double speed;      // The current speed of the bike.
    private int batteryLevel;  // The battery level of the bike, represented as a percentage (0 to 100).
    
    // Constructor for creating an EBike with a unique ID, initialized to available state and default location.
    public Ebike(String id) {
        this.id = id;
        this.state = EBikeState.AVAILABLE; 
        this.loc = new P2d(0,0); 
        direction = new V2d(1,0); 
        speed = 0; 
    }

    // Constructor for creating an EBike with a unique ID and specified position, initialized to available state.
    public Ebike(String id, P2d pos) {
        this.id = id;
        this.state = EBikeState.AVAILABLE; 
        this.loc = pos; 
        direction = new V2d(1,0); 
        speed = 0; 
    }

    // Constructor for creating an EBike with full specifications including ID, state, location, direction, speed, and battery level.
    public Ebike(String id, EBikeState eState, P2d loc, V2d direction, double speed, int batteryLevel) {
        this.id = id; 
        this.state = eState; 
        this.loc = loc; 
        this.direction = direction; 
        this.speed = speed; 
        this.batteryLevel = batteryLevel; 
    }
    
    // Returns the unique identifier of the bike.
    public String getId() {
        return id; 
    }

    // Returns the current state of the bike.
    public EBikeState getState() {
        return state; 
    }
    
    // Recharges the bike's battery to 100% and sets its state to available.
    public void rechargeBattery() {
        batteryLevel = 100; 
        state = EBikeState.AVAILABLE;
    }
    
    // Returns the current battery level of the bike.
    public int getBatteryLevel() {
        return batteryLevel; 
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel < 0 ? 0 : (batteryLevel > 100 ? 100 : batteryLevel);

        if (batteryLevel == 0 && state != EBikeState.DISMISSED) {
            state = EBikeState.MAINTENANCE; 
        } else if (batteryLevel > 0 && state == EBikeState.MAINTENANCE) {
            state = EBikeState.AVAILABLE; 
        }
    }

    // Decreases the battery level by the specified amount and updates the state if it drops below zero.
    public void decreaseBatteryLevel(int delta) {
        batteryLevel -= delta; 
        if (batteryLevel < 0) {
            batteryLevel = 0; 
            state = EBikeState.MAINTENANCE; 
        }
    }
    
    // Checks if the bike is currently available.
    public boolean isAvailable() {
        return state == EBikeState.AVAILABLE; 
    }

    // Checks if the bike is currently in use.
    public boolean isInUse() {
        return state == EBikeState.IN_USE; 
    }

    // Updates the bike's state to the provided state.
    public void updateState(EBikeState state) {
        this.state = state; 
    }
    
    // Updates the bike's location to the provided new location.
    public void updateLocation(P2d newLoc) {
        loc = newLoc; 
    }

    // Updates the bike's speed to the specified value.
    public void updateSpeed(double speed) {
        this.speed = speed; 
    }
    
    // Updates the bike's direction to the specified value.
    public void updateDirection(V2d dir) {
        this.direction = dir; 
    }
    
    // Returns the current speed of the bike.
    public double getSpeed() {
        return speed; 
    }
    
    // Returns the current direction of the bike.
    public V2d getDirection() {
        return direction; 
    }
    
    // Returns the current location of the bike.
    public P2d getLocation(){
        return loc; 
    }
    
    // Returns a string representation of the bike's current state.
    public String toString() {
        return "{ id: " + id + ", loc: " + loc + ", batteryLevel: " + batteryLevel + ", state: " + state + " }"; 
    }
}
