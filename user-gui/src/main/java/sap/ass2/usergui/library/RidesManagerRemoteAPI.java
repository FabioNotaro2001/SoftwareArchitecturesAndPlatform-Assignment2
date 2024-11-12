package sap.ass2.usergui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface RidesManagerRemoteAPI {
    Future<JsonObject> beginRide(String userID, String bikeID);
    Future<Void> stopRide(String rideID, String userID);
    Future<JsonObject> subscribeToRideEvents(String rideId, RideEventObserver observer);
    void unsubscribeFromRideEvents();
}