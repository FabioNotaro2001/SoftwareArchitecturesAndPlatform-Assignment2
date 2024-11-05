package sap.ass2.admingui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

public interface RidesManagerRemoteAPI {
    Future<JsonArray> getAllRides();
    Future<JsonArray> subscribeForRideEvents(RideEventObserver observer);
}