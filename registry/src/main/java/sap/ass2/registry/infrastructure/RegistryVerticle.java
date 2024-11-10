package sap.ass2.registry.infrastructure;

import java.net.URI;
import java.util.Optional;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import sap.ass2.registry.domain.RegistryAPI;

public class RegistryVerticle extends AbstractVerticle {
    private int port;
    private RegistryAPI registryAPI;
    
    public RegistryVerticle(int port, RegistryAPI registryAPI) {
        this.port = port;
        this.registryAPI = registryAPI;
    }

    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        router.route(HttpMethod.POST, "/api/registry/users-manager").handler(this::registerUsersManager);
        router.route(HttpMethod.POST, "/api/registry/ebikes-manager").handler(this::registerEbikesManager);
        router.route(HttpMethod.POST, "/api/registry/rides-manager").handler(this::registerRidesManager);
        router.route(HttpMethod.GET, "/api/registry/users-manager/:usersManagerName").handler(this::lookupUsersManager);
        router.route(HttpMethod.GET, "/api/registry/ebikes-manager/:ebikesManagerName").handler(this::lookupEbikesManager);
        router.route(HttpMethod.GET, "/api/registry/rides-manager/:ridesManagerName").handler(this::lookupRidesManager);
        
        server.requestHandler(router).listen(this.port);
    }

    private static void sendReply(HttpServerResponse response, JsonObject reply) {
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    private static void sendServiceError(HttpServerResponse response, Exception ex) {
        response.setStatusCode(500);
        response.putHeader("content-type", "application/json");
        response.end(ex.getMessage());
    }

    private static void sendBadRequest(HttpServerResponse response, Exception ex) {
        response.setStatusCode(400);
        response.putHeader("content-type", "application/json");
        response.end(ex.getMessage());
    }

    protected void registerUsersManager(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String name = data.getString("name");
			String address = data.getString("address");			

            JsonObject response = new JsonObject();
            try {
                var url = new URI(address).toURL();
                this.registryAPI.registerUsersManager(name, url);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void registerEbikesManager(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String name = data.getString("name");
			String address = data.getString("address");			

            JsonObject response = new JsonObject();
            try {
                var url = new URI(address).toURL();
                this.registryAPI.registerEbikesManager(name, url);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void registerRidesManager(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String name = data.getString("name");
			String address = data.getString("address");			

            JsonObject response = new JsonObject();
            try {
                var url = new URI(address).toURL();
                this.registryAPI.registerRidesManager(name, url);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void lookupUsersManager(RoutingContext context) {
        JsonObject response = new JsonObject();
        String name = context.pathParam("usersManagerName");
        try {
            var usersManagerOpt = this.registryAPI.lookupUsersManager(name);
            if (usersManagerOpt.isPresent()) {
                response.put("usersManager", usersManagerOpt.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void lookupEbikesManager(RoutingContext context) {
        JsonObject response = new JsonObject();
        String name = context.pathParam("ebikesManagerName");
        try {
            var ebikesManagerOpt = this.registryAPI.lookupEbikesManager(name);
            if (ebikesManagerOpt.isPresent()) {
                response.put("ebikesManager", ebikesManagerOpt.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void lookupRidesManager(RoutingContext context) {
        JsonObject response = new JsonObject();
        String name = context.pathParam("ridesManagerName");
        try {
            var ridesManagerOpt = this.registryAPI.lookupRidesManager(name);
            if (ridesManagerOpt.isPresent()) {
                response.put("ridesManager", ridesManagerOpt.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }
}
