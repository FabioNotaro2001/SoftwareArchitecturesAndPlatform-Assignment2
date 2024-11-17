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

public class UsersManagerProxy implements UsersManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL usersManagerAddress;
	private WebSocket webSocket;
	
	public UsersManagerProxy(URL usersManagerAddress) {
		if (Vertx.currentContext() != null) {
			vertx = Vertx.currentContext().owner();
		} else {
			vertx = Vertx.vertx();
		}
		
		this.usersManagerAddress = usersManagerAddress;
		HttpClientOptions options = new HttpClientOptions()
            .setDefaultHost(usersManagerAddress.getHost())
            .setDefaultPort(usersManagerAddress.getPort());
		client = vertx.createHttpClient(options);
	}

	@Override
	public Future<Optional<JsonObject>> getUserByID(String userID) {
		Promise<Optional<JsonObject>> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/users/" + userID)
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(Optional.ofNullable(obj.getJsonObject("user")));
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
	public Future<Void> decreaseCredit(String userID, int amount) {
		Promise<Void> p = Promise.promise();
		client
		.request(HttpMethod.POST, "/api/users/" + userID + "/decrease-credit")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					p.complete();
				});
			});
			req.putHeader("content-type", "application/json");
			JsonObject body = new JsonObject();
			body.put("userId", userID);
			body.put("credit", amount);
			
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
	public Future<JsonObject> subscribeToUserEvents(String userID, UserEventObserver observer) {
		Promise<JsonObject> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
			.setHost(this.usersManagerAddress.getHost())
			.setPort(this.usersManagerAddress.getPort())
			.setURI("/api/users/" + userID + "/events")
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
                        JsonObject user = obj.getJsonObject("user");
                        p.complete(user);
                    } else {
                        int credit = obj.getInteger("credit");
                        
                        observer.userUpdated(userID, credit);
                    } 
                });
            } else {
                p.fail(res.cause());
            }
		});
		
		return p.future();
	}

	@Override
	public void unsubscribeFromUserEvents() {
		this.webSocket.writeTextMessage("unsubscribe")
			.onComplete(h -> {
				this.webSocket.close();
				this.webSocket = null;
			});
	}
}