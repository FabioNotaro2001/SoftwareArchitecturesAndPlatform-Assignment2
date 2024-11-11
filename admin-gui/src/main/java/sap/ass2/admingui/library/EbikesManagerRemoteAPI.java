package sap.ass2.admingui.library;

import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface EbikesManagerRemoteAPI {
    Future<JsonArray> getAllEbikes();
    Future<JsonObject> createBike(String bikeID, double locationX, double locationY);
    Future<Void> removeBike(String bikeID);
    Future<Optional<JsonObject>> getBikeByID(String bikeID);
    Future<JsonArray> subscribeForEbikeEvents(EbikeEventObserver observer);
}