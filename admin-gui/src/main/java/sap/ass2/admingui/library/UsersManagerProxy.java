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

public class UsersManagerProxy implements UsersManagerRemoteAPI {
    private HttpClient client;
	private Vertx vertx;
	private URL usersManagerAddress;
	
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
    public Future<JsonArray> getAllUsers() {
        Promise<JsonArray> p = Promise.promise();
		client
		.request(HttpMethod.GET, "/api/users")
		.onSuccess(req -> {
			req.response().onSuccess(response -> {
				response.body().onSuccess(buf -> {
					JsonObject obj = buf.toJsonObject();
					p.complete(obj.getJsonArray("users"));
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
    public Future<JsonArray> subscribeToUsersEvents(UserEventObserver observer) {
        Promise<JsonArray> p = Promise.promise();
		
		WebSocketConnectOptions wsoptions = new WebSocketConnectOptions()
				  .setHost(this.usersManagerAddress.getHost())
				  .setPort(this.usersManagerAddress.getPort())
				  .setURI("/api/users-events")
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
                        JsonArray users = obj.getJsonArray("users");
                        p.complete(users);
                    } else if (evType.equals("user-update")) {
                        String userID = obj.getString("userId");
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

}
