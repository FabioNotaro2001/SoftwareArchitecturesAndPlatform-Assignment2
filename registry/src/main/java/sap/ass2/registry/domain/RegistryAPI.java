package sap.ass2.registry.domain;

import java.net.URL;
import java.util.Optional;

public interface RegistryAPI {
    void registerEbikeManager(String name, URL url);
    void registerRideManager(String name, URL url);
    void registerUserManager(String name, URL url);
    Optional<URL> lookupEbikeManager(String name);
    Optional<URL> lookupUserManager(String name);
    Optional<URL> lookupRideManager(String name);
}