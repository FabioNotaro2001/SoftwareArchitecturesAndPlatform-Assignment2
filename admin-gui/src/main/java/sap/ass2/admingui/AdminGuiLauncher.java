package sap.ass2.admingui;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import io.vertx.core.Future;
import sap.ass2.admingui.gui.AdminGUI;
import sap.ass2.admingui.library.*;

public class AdminGuiLauncher {
    public static void main(String[] args) throws MalformedURLException {
        // TODO: la admin gui non mostra subito le bici nello spazio, ma compaiono se abbassi la finestra e la riapri.
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
            
                    AdminGUI gui = new AdminGUI(ebikesManager, ridesManager, usersManager);
                    gui.display();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            });
    }
}
