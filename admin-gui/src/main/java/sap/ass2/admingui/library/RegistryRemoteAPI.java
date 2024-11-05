package sap.ass2.admingui.library;

import java.net.URL;
import java.util.Optional;
import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Optional<URL>> lookupUserManager(String name);
    Future<Optional<URL>> lookupRideManager(String name);
    Future<Optional<URL>> lookupEbikeManager(String name);
}