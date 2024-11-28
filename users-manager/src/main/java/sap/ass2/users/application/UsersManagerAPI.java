package sap.ass2.users.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.users.domain.RepositoryException;
import sap.ass2.users.domain.UserEventObserver;

public interface UsersManagerAPI {
    JsonArray getAllUsers();

    JsonObject createUser(String userID) throws RepositoryException;

    Optional<JsonObject> getUserByID(String userID);

    void rechargeCredit(String userID, int credit) throws RepositoryException, IllegalArgumentException;

    void decreaseCredit(String userID, int amount) throws RepositoryException;

    void subscribeToUserEvents(UserEventObserver observer);
}
