package sap.ass2.usergui.library;

import java.net.URL;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
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
}