package sap.ass2.usergui.library;

import java.net.URL;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;

public class RidesManagerProxy implements RidesManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ridesManagerAddress;
	private WebSocket webSocket;
	
	public RidesManagerProxy(URL ridesManagerAddress) {
		if (Vertx.currentContext() != null) {
			vertx = Vertx.currentContext().owner();
		} else {
			vertx = Vertx.vertx();
		}
		
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

	@Override
	public Future<JsonObject> subscribeToRideEvents(String rideId, RideEventObserver observer) {
		Promise<JsonObject> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
				  .setHost(this.ridesManagerAddress.getHost())
				  .setPort(this.ridesManagerAddress.getPort())
				  .setURI("/api/rides/" + rideId + "/events")
				  .setAllowOriginHeader(false);
		
		client
		.webSocket(wsoptions)
		.onComplete(res -> {
            if (res.succeeded()) {
                this.webSocket = res.result();
                System.out.println("Connected!");
                this.webSocket.textMessageHandler(data -> {
                    JsonObject obj = new JsonObject(data);
                    String evType = obj.getString("event");
                    if (evType.equals("subscription-started")) {
                        JsonObject ebike = obj.getJsonObject("ebike");
                        p.complete(ebike);
                    } else if (evType.equals("ride-step")) {
						Double x = obj.getDouble("x");
                        Double y = obj.getDouble("y");
						Double directionX = obj.getDouble("dirX");
						Double directionY = obj.getDouble("dirY");
						Double speed = obj.getDouble("speed");
						Integer batteryLevel = obj.getInteger("batteryLevel");
						
						observer.rideStep(rideId, x, y, directionX, directionY, speed, batteryLevel);
                    } else if (evType.equals("ride-end")) {
                        String reason = obj.getString("reason");

                        observer.rideEnded(rideId, reason);
                    }
					// The "ride-start" event can never happen.
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
	}

	@Override
	public void unsubscribeFromRideEvents() {
		this.webSocket.writeTextMessage("unsubscribe")
			.onComplete(h -> {
				this.webSocket.close();
				this.webSocket = null;
			});
	}
}
