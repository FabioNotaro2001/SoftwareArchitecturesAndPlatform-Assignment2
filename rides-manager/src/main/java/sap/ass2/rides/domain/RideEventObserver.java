package sap.ass2.rides.domain;

public interface RideEventObserver {
    void rideStarted(String rideID, String userID, String bikeID);
    void rideEnded(String rideID);
}
