package sap.ass2.rides.application;

import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface UsersManagerRemoteAPI {
    Future<Optional<JsonObject>> getUserByID(String userID);
    Future<Void> decreaseCredit(String userID, int amount);
}
