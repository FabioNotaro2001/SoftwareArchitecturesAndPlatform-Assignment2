package sap.ass2.rides.application;

import java.net.URL;
import java.util.Optional;

import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Void> registerRidesManager(String name, URL address);

    Future<Optional<String>> lookupEbikesManager(String name);
    Future<Optional<String>> lookupUsersManager(String name);
}