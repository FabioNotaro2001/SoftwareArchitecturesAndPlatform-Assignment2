package sap.ass2.users;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sap.ass2.users.domain.RepositoryException;

public class UsersManagerLauncher {
    private static final String SERVICE_ADDRESS = "http://localhost:9100";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, RepositoryException{
        URL localAddress = new URI(SERVICE_ADDRESS).toURL();
        UsersManagerService service = new UsersManagerService(localAddress);
        service.launch();
    }
}
