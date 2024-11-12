package sap.ass2.admingui.library;

public interface RideEventObserver {
    void rideStarted(String rideID, String userID, String bikeID);
    void rideStep(String rideID, double x, double y, int batteryLevel);
    void rideEnded(String rideID, String reason);
}
