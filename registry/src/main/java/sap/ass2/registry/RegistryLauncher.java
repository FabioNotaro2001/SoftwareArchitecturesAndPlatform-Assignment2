package sap.ass2.registry;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class RegistryLauncher {
    private static final String SERVICE_ADDRESS = "http://localhost:9000";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        URL localAddress = new URI(SERVICE_ADDRESS).toURL();
        RegistryService service = new RegistryService(localAddress);
        service.launch();
    }
}
