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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EbikesProxy implements EbikesAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL ebikesManagerAddress;
	private WebSocket webSocket;
	
	public EbikesProxy(URL appAddress) {
		if (Vertx.currentContext() != null) {
			vertx = Vertx.currentContext().owner();
		} else {
			vertx = Vertx.vertx();
		}
		
		this.ebikesManagerAddress = appAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(appAddress.getHost())
            .setDefaultPort(appAddress.getPort());
		client = vertx.createHttpClient(options);
	}

    @Override
    public Future<JsonArray> getAllAvailableEbikesIDs() {
        Promise<JsonArray> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/ebikes/ids")
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
	public Future<JsonObject> subscribeToEbikeEvents(String ebikeID, EbikeEventObserver observer) {
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
                    }
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
	}

	@Override
	public void unsubscribeFromEbikeEvents() {
        this.webSocket.writeTextMessage("unsubscribe")
			.onComplete(h -> {
				this.webSocket.close();
				this.webSocket = null;
			});
	}
}