package sap.ass2.registry.domain;

import java.net.URL;
import java.util.Optional;

public interface RegistryAPI {
    void registerUsersManager(String name, URL address);
    void registerEbikesManager(String name, URL address);
    void registerRidesManager(String name, URL address);
    Optional<String> lookupUsersManager(String name);
    Optional<String> lookupEbikesManager(String name);
    Optional<String> lookupRidesManager(String name);
}