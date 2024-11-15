package sap.ass2.rides;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import io.vertx.core.Future;
import sap.ass2.rides.application.EbikesManagerProxy;
import sap.ass2.rides.application.EbikesManagerRemoteAPI;
import sap.ass2.rides.application.RegistryProxy;
import sap.ass2.rides.application.RegistryRemoteAPI;
import sap.ass2.rides.application.UsersManagerProxy;
import sap.ass2.rides.application.UsersManagerRemoteAPI;

public class RidesManagerLauncher {
    private static final String RIDES_MANAGER_NAME = "rides-manager";
    private static final String SERVICE_ADDRESS = "http://localhost:9300";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        URL localAddress = URI.create(SERVICE_ADDRESS).toURL();

        RegistryRemoteAPI registry = new RegistryProxy(URI.create("http://localhost:9000").toURL());
        
        var usersFut = registry.lookupUsersManager("users-manager");
        var ebikesFut = registry.lookupEbikesManager("ebikes-manager");

        Future.all(usersFut, ebikesFut)
            .onSuccess(cf -> {
                List<Optional<String>> results = cf.list();

                var usersManagerAddressOpt = results.get(0);
                if (usersManagerAddressOpt.isEmpty()) {
                    System.err.println("Users manager not found.");
                    System.exit(1);
                }
                UsersManagerRemoteAPI usersManager = null;
                try {
                    usersManager = new UsersManagerProxy(URI.create(usersManagerAddressOpt.get()).toURL());
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                
                var ebikesManagerAddressOpt = results.get(1);
                if (ebikesManagerAddressOpt.isEmpty()) {
                    System.err.println("Ebikes manager not found.");
                    System.exit(1);
                }
                EbikesManagerRemoteAPI ebikesManager = null;
                try {
                    ebikesManager = new EbikesManagerProxy(URI.create(ebikesManagerAddressOpt.get()).toURL());
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                
                RidesManagerService service = new RidesManagerService(localAddress, usersManager, ebikesManager);
                service.launch();
                
                registry.registerRidesManager(RIDES_MANAGER_NAME, localAddress);
            });
    }
}
