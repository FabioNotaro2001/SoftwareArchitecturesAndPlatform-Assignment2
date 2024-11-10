package sap.ass2.admingui.library;

import java.net.URL;
import java.util.Optional;
import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Optional<String>> lookupUsersManager(String name);
    Future<Optional<String>> lookupRidesManager(String name);
    Future<Optional<String>> lookupEbikesManager(String name);
}