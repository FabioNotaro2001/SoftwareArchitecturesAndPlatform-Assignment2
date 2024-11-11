package sap.ass2.rides.application;

import java.net.URL;
import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Void> registerRidesManager(String name, URL address);
}