package sap.ass2.users;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import sap.ass2.users.application.RegistryProxy;
import sap.ass2.users.application.RegistryRemoteAPI;
import sap.ass2.users.domain.RepositoryException;

public class UsersManagerLauncher {
    private static final String USERS_MANAGER_NAME = "users-manager";
    private static final String SERVICE_ADDRESS = "http://localhost:9100";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, RepositoryException {
        URL localAddress = URI.create(SERVICE_ADDRESS).toURL();
        UsersManagerService service = new UsersManagerService(localAddress);
        service.launch();

        RegistryRemoteAPI registry = new RegistryProxy(URI.create("http://localhost:9000").toURL());
        registry.registerUsersManager(USERS_MANAGER_NAME, localAddress);
    }
}
