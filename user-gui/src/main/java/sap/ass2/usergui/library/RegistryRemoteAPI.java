package sap.ass2.usergui.library;

import java.net.URL;
import java.util.Optional;

import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Optional<URL>> lookupEbikeManager(String name);
    Future<Optional<URL>> lookupUserManager(String name);
    Future<Optional<URL>> lookupRideManager(String name);
}