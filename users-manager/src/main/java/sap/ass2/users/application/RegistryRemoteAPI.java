package sap.ass2.users.application;

import java.net.URL;
import io.vertx.core.Future;

public interface RegistryRemoteAPI {
    Future<Void> registerUsersManager(String name, URL address);
}