package sap.ass2.usergui.library;

import java.util.Optional;

import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Optional<String>> lookupEbikesManager(String name);
    Future<Optional<String>> lookupUsersManager(String name);
    Future<Optional<String>> lookupRidesManager(String name);
}