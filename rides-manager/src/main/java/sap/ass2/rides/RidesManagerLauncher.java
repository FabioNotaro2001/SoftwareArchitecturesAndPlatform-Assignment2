package sap.ass2.rides;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class RidesManagerLauncher {
    private static final String SERVICE_ADDRESS = "http://localhost:9300";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        URL localAddress = new URI(SERVICE_ADDRESS).toURL();
        RidesManagerService service = new RidesManagerService(localAddress);
        service.launch();
    }
}
