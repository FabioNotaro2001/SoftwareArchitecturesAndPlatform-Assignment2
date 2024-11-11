package sap.ass2.usergui.library;

import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface UsersManagerRemoteAPI {
    Future<JsonArray> getAllUsers();
    Future<JsonObject> createUser(String userID);
    Future<Optional<JsonObject>> getUserByID(String userID);
    Future<Void> rechargeCredit(String userID, int credit);
    Future<JsonObject> subscribeForUserEvents(String userID, UserEventObserver observer);
}
