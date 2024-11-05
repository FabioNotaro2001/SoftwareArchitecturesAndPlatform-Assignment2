package sap.ass2.rides.application;

import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.rides.domain.RideEventObserver;

public interface RidesManagerAPI {
    JsonArray getAllRides();
    JsonObject beginRide(String userID, String bikeID);
    void stopRide(String rideID, String userID);
    Optional<JsonObject> getRideByRideID(String rideID);
    Optional<JsonObject> getRideByBikeID(String bikeID);
    Optional<JsonObject> getRideByUserID(String userID);
    void subscribeForRideEvents(RideEventObserver observer);
}