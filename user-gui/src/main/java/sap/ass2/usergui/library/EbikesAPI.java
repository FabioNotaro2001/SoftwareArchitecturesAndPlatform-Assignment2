package sap.ass2.usergui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface EbikesAPI {
    Future<JsonArray> getAllAvailableEbikesIDs();
    Future<JsonObject> subscribeToEbikeEvents(String ebikeID, EbikeEventObserver observer);
    void unsubscribeFromEbikeEvents();
}