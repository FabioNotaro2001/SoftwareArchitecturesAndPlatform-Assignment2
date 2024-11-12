package sap.ass2.usergui.library;

public interface RideEventObserver {
    void rideStep(String rideID, double x, double y, int batteryLevel);
    void rideEnded(String rideID, String reason);
}
