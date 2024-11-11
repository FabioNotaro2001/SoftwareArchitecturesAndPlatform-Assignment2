package sap.ass2.rides.application;

import java.net.URL;
import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import sap.ass2.rides.domain.Ebike.EbikeState;

public class EbikesManagerProxy implements EbikesManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ebikesManagerAddress;
    private WebSocket webSocket;
	
	public EbikesManagerProxy(URL ebikesManagerAddress) {
		vertx = Vertx.vertx();
		this.ebikesManagerAddress = ebikesManagerAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(ebikesManagerAddress.getHost())
            .setDefaultPort(ebikesManagerAddress.getPort());
		client = vertx.createHttpClient(options);
	}

    @Override
    public Future<Optional<JsonObject>> getBikeByID(String bikeID) {
        Promise<Optional<JsonObject>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/ebikes/" + bikeID)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
                    p.complete(Optional.of(obj.getJsonObject("ebike")));
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
    public Future<Void> updateBike(String bikeID, Optional<EbikeState> state, Optional<Double> locationX,
            Optional<Double> locationY, Optional<Double> directionX, Optional<Double> directionY,
            Optional<Double> speed, Optional<Integer> batteryLevel) {
            Promise<Void> p = Promise.promise();
            client
            .request(HttpMethod.POST, "/api/ebikes/" + bikeID)
            .onSuccess(req -> {
                req.response().onSuccess(response -> {
                    response.body().onSuccess(buf -> {
                        p.complete();
                    });
                });
                req.putHeader("content-type", "application/json");
                JsonObject body = new JsonObject();
                body.put("ebikeId", bikeID);
                state.ifPresent(s -> body.put("state", s.toString()));
                locationX.ifPresent(locX -> body.put("x", locX));
                locationY.ifPresent(locY -> body.put("y", locY));
                directionX.ifPresent(dirX -> body.put("dirX", dirX));
                directionY.ifPresent(dirY -> body.put("dirY", dirY));
                speed.ifPresent(s -> body.put("speed", s));
                batteryLevel.ifPresent(bl -> body.put("batteryLevel", bl));
                
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
    public Future<JsonObject> subscribeForEbikeEvents(String ebikeID, EbikeEventObserver observer) {
        Promise<JsonObject> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
            .setHost(this.ebikesManagerAddress.getHost())
            .setPort(this.ebikesManagerAddress.getPort())
            .setURI("/api/ebikes/" + ebikeID + "/events")
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
                    } else if (evType.equals("ebike-update")) {
                        String state = obj.getString("state");
                        double locX = obj.getDouble("x");
                        double locY = obj.getDouble("y");
                        double dirX = obj.getDouble("dirX");
                        double dirY = obj.getDouble("dirY");
                        double speed = obj.getDouble("speed");
                        int batteryLevel = obj.getInteger("batteryLevel");
                        
                        observer.bikeUpdated(ebikeID, EbikeState.valueOf(state), locX, locY, dirX, dirY, speed, batteryLevel);
                    } else if (evType.equals("ebike-remove")) {
                        observer.bikeRemoved(ebikeID);
                    }
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
    }

    @Override
    public void unsubscribeForEbikeEvents(String bikeID, EbikeEventObserver observer) {
        this.webSocket.writeTextMessage("unsubscribe");
        this.webSocket.close();
        this.webSocket = null;
    }

}
