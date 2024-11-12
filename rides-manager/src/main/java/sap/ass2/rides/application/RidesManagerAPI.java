package sap.ass2.rides.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.rides.domain.RideEventObserver;

public interface RidesManagerAPI {
    JsonArray getAllRides();
    JsonObject beginRide(String userID, String ebikeID) throws IllegalArgumentException;
    void stopRide(String rideID, String userID) throws IllegalArgumentException;
    Optional<JsonObject> getRideByRideID(String rideID);
    Optional<JsonObject> getRideByEbikeID(String ebikeID);
    Optional<JsonObject> getRideByUserID(String userID);
    void subscribeForRideEvents(RideEventObserver observer);
    void subscribeForRideEvents(String rideId, RideEventObserver observer);
    void unsubscribeForRideEvents(String rideId, RideEventObserver observer);
}