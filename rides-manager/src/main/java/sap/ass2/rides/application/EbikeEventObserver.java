package sap.ass2.rides.application;

public interface EbikeEventObserver {
    void bikeUpdated(String bikeID, EbikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int betteryLevel);
    void bikeRemoved(String bikeID);
}
