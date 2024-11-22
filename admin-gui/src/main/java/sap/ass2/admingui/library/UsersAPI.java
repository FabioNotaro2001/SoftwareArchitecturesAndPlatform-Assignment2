package sap.ass2.admingui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

public interface UsersAPI {
    Future<JsonArray> getAllUsers();
    Future<JsonArray> subscribeToUsersEvents(UserEventObserver observer);
}
