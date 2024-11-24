package sap.ass2.ebikes;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sap.ass2.ebikes.application.RegistryProxy;
import sap.ass2.ebikes.application.RegistryRemoteAPI;
import sap.ass2.ebikes.domain.RepositoryException;

public class EbikesManagerLauncher {
    private static final String EBIKES_MANAGER_NAME = "ebikes-manager";
    private static final String SERVICE_ADDRESS = "http://localhost:9200";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, RepositoryException{
        URL localAddress = URI.create(SERVICE_ADDRESS).toURL();
        EbikesManagerService service = new EbikesManagerService(localAddress);
        service.launch();

        RegistryRemoteAPI registry = new RegistryProxy(URI.create("http://localhost:9000").toURL());
        registry.registerEbikesManager(EBIKES_MANAGER_NAME, localAddress);
    }
}