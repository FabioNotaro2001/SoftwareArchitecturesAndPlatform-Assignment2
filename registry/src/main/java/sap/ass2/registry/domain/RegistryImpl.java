package sap.ass2.registry.domain;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryImpl implements RegistryAPI {
    // Maps that contain the (name, URL) pair for each service.
    private Map<String, URL> ebikesManagers;
    private Map<String, URL> ridesManagers;
    private Map<String, URL> usersManagers;

    public RegistryImpl() {
        this.ebikesManagers = new ConcurrentHashMap<>();
        this.ridesManagers = new ConcurrentHashMap<>();
        this.usersManagers = new ConcurrentHashMap<>();
    }

    @Override
    public void registerUsersManager(String name, URL address) {
        this.usersManagers.put(name, address);
    }

    @Override
    public void registerEbikesManager(String name, URL address) {
        this.ebikesManagers.put(name, address);
    }

    @Override
    public void registerRidesManager(String name, URL address) {
        this.ridesManagers.put(name, address);
    }

    @Override
    public Optional<String> lookupUsersManager(String name) {
        return Optional.ofNullable(this.usersManagers.get(name)).map(URL::toString);
    }

    @Override
    public Optional<String> lookupEbikesManager(String name) {
        return Optional.ofNullable(this.ebikesManagers.get(name)).map(URL::toString);
    }

    @Override
    public Optional<String> lookupRidesManager(String name) {
        return Optional.ofNullable(this.ridesManagers.get(name)).map(URL::toString);
    }
}
