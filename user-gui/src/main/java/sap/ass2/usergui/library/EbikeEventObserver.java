package sap.ass2.usergui.library;

public interface EbikeEventObserver {
    void bikeUpdated(String ebikeID, EbikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int betteryLevel);
}
