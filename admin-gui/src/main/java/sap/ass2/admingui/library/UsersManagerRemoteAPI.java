package sap.ass2.admingui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

public interface UsersManagerRemoteAPI {
    Future<JsonArray> getAllUsers();
    Future<JsonArray> subscribeToUsersEvents(UserEventObserver observer);
}
