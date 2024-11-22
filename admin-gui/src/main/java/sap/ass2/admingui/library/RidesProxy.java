package sap.ass2.admingui.library;

import java.net.URL;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RidesProxy implements RidesAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ridesManagerAddress;
	
	public RidesProxy(URL appAddress) {
        if (Vertx.currentContext() != null) {
			vertx = Vertx.currentContext().owner();
		} else {
			vertx = Vertx.vertx();
		}
        
		this.ridesManagerAddress = appAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(appAddress.getHost())
            .setDefaultPort(appAddress.getPort());
		client = vertx.createHttpClient(options);
	}

    @Override
    public Future<JsonArray> getAllRides() {
        Promise<JsonArray> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/rides")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(obj.getJsonArray("rides"));
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
    public Future<JsonArray> subscribeToRideEvents(RideEventObserver observer) {
        Promise<JsonArray> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
				  .setHost(this.ridesManagerAddress.getHost())
				  .setPort(this.ridesManagerAddress.getPort())
				  .setURI("/api/rides/events")
				  .setAllowOriginHeader(false);
		
		client
		.webSocket(wsoptions)
		.onComplete(res -> {
            if (res.succeeded()) {
                WebSocket ws = res.result();
                System.out.println("Connected!");
                ws.textMessageHandler(data -> {
                    JsonObject obj = new JsonObject(data);
                    String evType = obj.getString("event");
                    if (evType.equals("subscription-started")) {
                        JsonArray ebikes = obj.getJsonArray("rides");
                        p.complete(ebikes);
                    } else if (evType.equals("ride-start")) {
                        String rideID = obj.getString("rideId");
                        String userID = obj.getString("userId");
                        String ebikeID = obj.getString("ebikeId");
                        
                        observer.rideStarted(rideID, userID, ebikeID);
                    } else if (evType.equals("ride-step")) {
                        String rideID = obj.getString("rideId");
                        Double x = obj.getDouble("x");
                        Double y = obj.getDouble("y");
                        Double directionX = obj.getDouble("dirX");
                        Double directionY = obj.getDouble("dirY");
                        Double speed = obj.getDouble("speed");
                        Integer batteryLevel = obj.getInteger("batteryLevel");
                        
                        observer.rideStep(rideID, x, y, directionX, directionY, speed, batteryLevel);
                    } else if (evType.equals("ride-end")) {
                        String rideID = obj.getString("rideId");
                        String reason = obj.getString("reason");
                        
                        observer.rideEnded(rideID, reason);
                    }
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
    }

}
