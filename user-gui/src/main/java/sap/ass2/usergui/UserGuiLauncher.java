package sap.ass2.usergui;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import io.vertx.core.Future;
import sap.ass2.usergui.gui.UserGUI;
import sap.ass2.usergui.library.EbikesManagerProxy;
import sap.ass2.usergui.library.EbikesManagerRemoteAPI;
import sap.ass2.usergui.library.RegistryProxy;
import sap.ass2.usergui.library.RegistryRemoteAPI;
import sap.ass2.usergui.library.RidesManagerProxy;
import sap.ass2.usergui.library.RidesManagerRemoteAPI;
import sap.ass2.usergui.library.UsersManagerProxy;
import sap.ass2.usergui.library.UsersManagerRemoteAPI;

public class UserGuiLauncher {
    public static void main(String[] args) throws MalformedURLException {
        RegistryRemoteAPI registry = new RegistryProxy(URI.create("http://localhost:9000").toURL());
        
        var usersFut = registry.lookupUsersManager("users-manager");
        var ebikesFut = registry.lookupEbikesManager("ebikes-manager");
        var ridesFut = registry.lookupRidesManager("rides-manager");

        Future.all(usersFut, ebikesFut, ridesFut)
            .onSuccess(cf -> {
                try {
                    List<Optional<String>> results = cf.list();

                    var usersManagerAddressOpt = results.get(0);
                    if (usersManagerAddressOpt.isEmpty()) {
                        System.err.println("Users manager not found.");
                        System.exit(1);
                    }
                    UsersManagerRemoteAPI usersManager = new UsersManagerProxy(URI.create(usersManagerAddressOpt.get()).toURL());
                    
                    var ebikesManagerAddressOpt = results.get(1);
                    if (ebikesManagerAddressOpt.isEmpty()) {
                        System.err.println("Ebikes manager not found.");
                        System.exit(1);
                    }
                    EbikesManagerRemoteAPI ebikesManager = new EbikesManagerProxy(URI.create(ebikesManagerAddressOpt.get()).toURL());
                    
                    var ridesManagerAddressOpt = results.get(2);
                    if (ridesManagerAddressOpt.isEmpty()) {
                        System.err.println("Rides manager not found.");
                        System.exit(1);
                    }
                    RidesManagerRemoteAPI ridesManager = new RidesManagerProxy(URI.create(ridesManagerAddressOpt.get()).toURL());
            
                    UserGUI gui = new UserGUI(usersManager, ridesManager, ebikesManager);
                    gui.display();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            });
    }
}
