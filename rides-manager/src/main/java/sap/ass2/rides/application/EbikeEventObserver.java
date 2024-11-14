package sap.ass2.rides.application;

import sap.ass2.rides.domain.EbikeState;

public interface EbikeEventObserver {
    void bikeUpdated(String bikeID, EbikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int batteryLevel);
    void bikeRemoved(String bikeID);
}
