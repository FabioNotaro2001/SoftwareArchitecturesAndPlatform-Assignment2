package sap.ass2.admingui.library;

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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EbikesManagerProxy implements EbikesManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ebikesManagerAddress;
	
	public EbikesManagerProxy(URL ebikesManagerAddress) {
		vertx = Vertx.vertx();
		this.ebikesManagerAddress = ebikesManagerAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(ebikesManagerAddress.getHost())
            .setDefaultPort(ebikesManagerAddress.getPort());
		client = vertx.createHttpClient(options);
	}

    @Override
    public Future<JsonArray> getAllEbikes() {
        Promise<JsonArray> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/ebikes")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(obj.getJsonArray("ebikes"));
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
    public Future<JsonObject> createBike(String ebikeID, double locationX, double locationY) {
        Promise<JsonObject> p = Promise.promise();
		client
		.request(HttpMethod.POST, "/api/ebikes")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(obj.getJsonObject("ebike"));
				});
			});
            req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
			body.put("ebikeId", ebikeID);
			body.put("x", locationX);
			body.put("y", locationY);

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
    public Future<Void> removeBike(String ebikeID) {
        Promise<Void> p = Promise.promise();
		client
		.request(HttpMethod.DELETE, "/api/ebikes/" + ebikeID)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					p.complete();
				});
			});
            req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
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
    public Future<Optional<JsonObject>> getBikeByID(String ebikeID) {
        Promise<Optional<JsonObject>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/ebikes/" + ebikeID)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(Optional.of(obj.getJsonObject("ebike")));
				});
			});
            req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
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
    public Future<JsonArray> subscribeToEbikeEvents(EbikeEventObserver observer) {
        Promise<JsonArray> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
				  .setHost(this.ebikesManagerAddress.getHost())
				  .setPort(this.ebikesManagerAddress.getPort())
				  .setURI("/api/ebikes/events")
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
                        JsonArray ebikes = obj.getJsonArray("ebikes");
                        p.complete(ebikes);
                    } else if (evType.equals("ebike-update")) {
                        String ebikeID = obj.getString("ebikeId");
                        String state = obj.getString("state");
                        double locX = obj.getDouble("x");
                        double locY = obj.getDouble("y");
                        double dirX = obj.getDouble("dirX");
                        double dirY = obj.getDouble("dirY");
                        double speed = obj.getDouble("speed");
                        int batteryLevel = obj.getInteger("batteryLevel");
                        
                        observer.bikeUpdated(ebikeID, EbikeState.valueOf(state), locX, locY, dirX, dirY, speed, batteryLevel);
                    } else if (evType.equals("ebike-remove")) {
                        String ebikeID = obj.getString("ebikeId");
                        
                        observer.bikeRemoved(ebikeID);
                    }
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
    }

}
