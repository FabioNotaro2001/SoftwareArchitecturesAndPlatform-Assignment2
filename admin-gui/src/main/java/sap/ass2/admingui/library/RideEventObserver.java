package sap.ass2.admingui.library;

public interface RideEventObserver {
    void rideStarted(String rideID, String userID, String bikeID);
    void rideEnded(String rideID);
}
