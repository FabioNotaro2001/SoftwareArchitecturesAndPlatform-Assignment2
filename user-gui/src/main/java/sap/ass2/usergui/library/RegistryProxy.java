package sap.ass2.usergui.library;

import java.net.URL;
import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class RegistryProxy implements RegistryRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	
	public RegistryProxy(URL registryAddress) {
		vertx = Vertx.vertx();
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(registryAddress.getHost())
            .setDefaultPort(registryAddress.getPort());
		client = vertx.createHttpClient(options);
	}
    
    @Override
    public Future<Optional<String>> lookupUsersManager(String name) {
        Promise<Optional<String>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/registry/users-manager/" + name)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(Optional.of(obj.getString("usersManager")));
				});
			});
			req.send();
		})
		.onFailure(f -> {
			p.fail(f.getMessage());
		});
		return p.future();
    }

    @Override
    public Future<Optional<String>> lookupRidesManager(String name) {
        Promise<Optional<String>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/registry/rides-manager/" + name)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(Optional.of(obj.getString("ridesManager")));
				});
			});
			req.send();
		})
		.onFailure(f -> {
			p.fail(f.getMessage());
		});
		return p.future();
    }

    @Override
    public Future<Optional<String>> lookupEbikesManager(String name) {
        Promise<Optional<String>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/registry/ebikes-manager/" + name)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(Optional.of(obj.getString("ebikesManager")));
				});
			});
			req.send();
		})
		.onFailure(f -> {
			p.fail(f.getMessage());
		});
		return p.future();
    }

}
