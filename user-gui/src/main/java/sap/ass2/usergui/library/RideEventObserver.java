package sap.ass2.usergui.library;

public interface RideEventObserver {
    void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel);
    void rideEnded(String rideID, String reason);
}
