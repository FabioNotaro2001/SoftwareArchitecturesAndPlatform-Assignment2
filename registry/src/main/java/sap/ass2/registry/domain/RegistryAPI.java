package sap.ass2.registry.domain;

import java.net.URL;
import java.util.Optional;

public interface RegistryAPI {
    void registerUsersManager(String name, URL address);
    void registerEbikesManager(String name, URL address);
    void registerRidesManager(String name, URL address);
    Optional<URL> lookupUsersManager(String name);
    Optional<URL> lookupEbikesManager(String name);
    Optional<URL> lookupRidesManager(String name);
}