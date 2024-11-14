package sap.ass2.ebikes;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sap.ass2.ebikes.domain.RepositoryException;

public class EbikesManagerLauncher {
    private static final String SERVICE_ADDRESS = "http://localhost:9200";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, RepositoryException{
        URL localAddress = new URI(SERVICE_ADDRESS).toURL();
        EbikesManagerService service = new EbikesManagerService(localAddress);
        service.launch();

        // TODO: create registry proxy and register, and other proxies
    }
}