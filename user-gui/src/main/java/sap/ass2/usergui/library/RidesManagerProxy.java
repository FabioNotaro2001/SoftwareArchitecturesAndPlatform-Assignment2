package sap.ass2.usergui.library;

import java.net.URL;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class RidesManagerProxy implements RidesManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ridesManagerAddress;
	
	public RidesManagerProxy(URL ridesManagerAddress) {
		vertx = Vertx.vertx();
		this.ridesManagerAddress = ridesManagerAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(ridesManagerAddress.getHost())
            .setDefaultPort(ridesManagerAddress.getPort());
		client = vertx.createHttpClient(options);
	}

    @Override
    public Future<JsonObject> beginRide(String userID, String ebikeID) {
        Promise<JsonObject> p = Promise.promise();
		client
		.request(HttpMethod.POST, "/api/rides")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(obj.getJsonObject("ride"));
				});
			});
            req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
			body.put("userId", userID);
			body.put("ebikeId", ebikeID);

			String payload = body.encodePrettily();
		    req.putHeader("content-length", "" + payload.length());
			req.write(payload);
			req.send();
		})
		.onFailure(f -> {
			p.fail(f.getMessage());
		});
		return p.future();
    }

    @Override
    public Future<Void> stopRide(String rideID, String userID) {
        Promise<Void> p = Promise.promise();
		client
		.request(HttpMethod.DELETE, "/api/rides")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					p.complete();
				});
			});
            req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
			body.put("rideId", rideID);
			body.put("userId", userID);
			
			String payload = body.encodePrettily();
		    req.putHeader("content-length", "" + payload.length());
			req.write(payload);
			req.send();
		})
		.onFailure(f -> {
			p.fail(f.getMessage());
		});
		return p.future();
    }
}
