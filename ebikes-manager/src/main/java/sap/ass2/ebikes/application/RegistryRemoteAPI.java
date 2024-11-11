package sap.ass2.ebikes.application;

import java.net.URL;
import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Void> registerEbikesManager(String name, URL address);
}