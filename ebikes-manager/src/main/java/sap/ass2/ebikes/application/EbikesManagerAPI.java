package sap.ass2.ebikes.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.ebikes.domain.EbikeEventObserver;
import sap.ass2.ebikes.domain.EBike.EBikeState;

public interface EbikesManagerAPI {
    JsonArray getAllBikes();
    JsonArray getAllAvailableBikesIDs();
    JsonObject createBike(String bikeID, double locationX, double locationY);
    void removeBike(String bikeID);
    Optional<JsonObject> getBikeByID(String bikeID);
    void updateBike(String bikeID, Optional<EBikeState> state, Optional<Double> locationX, Optional<Double> locationY, Optional<Double> directionX, Optional<Double> directionY, Optional<Double> speed, Optional<Integer> batteryLevel);
    void subscribeForEbikeEvents(EbikeEventObserver observer);
}