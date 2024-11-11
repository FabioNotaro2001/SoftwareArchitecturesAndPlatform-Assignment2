package sap.ass2.usergui.library;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

public interface EbikesManagerRemoteAPI {
    Future<JsonArray> getAllAvailableEbikesIDs();
}