package sap.ass2.admingui.library;

public interface EbikeEventObserver {
    void bikeUpdated(String bikeID, EBikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int betteryLevel);
    void bikeRemoved(String bikeID);
}
