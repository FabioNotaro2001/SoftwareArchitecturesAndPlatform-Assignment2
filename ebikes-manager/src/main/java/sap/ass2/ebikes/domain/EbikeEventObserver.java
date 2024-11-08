package sap.ass2.ebikes.domain;

import sap.ass2.ebikes.domain.Ebike.EbikeState;

public interface EbikeEventObserver {
    void bikeUpdated(String bikeID, EbikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int betteryLevel);
    void bikeRemoved(String bikeID);
}
