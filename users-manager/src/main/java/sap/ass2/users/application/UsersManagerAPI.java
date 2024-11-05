package sap.ass2.users.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.users.domain.UserEventObserver;

public interface UsersManagerAPI {
    JsonArray getAllUsers();
    JsonObject createUser(String userID);
    Optional<JsonObject> getUserByID(String userID);
    void rechargeCredit(String userID, int credit);
    void subscribeForUserEvents(UserEventObserver observer);
    void subscribeForUserEvents(String userID, UserEventObserver observer);
}
