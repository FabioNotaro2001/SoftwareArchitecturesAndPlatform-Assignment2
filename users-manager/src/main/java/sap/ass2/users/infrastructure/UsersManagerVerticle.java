package sap.ass2.users.infrastructure;

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
import sap.ass2.users.application.UsersManagerAPI;
import sap.ass2.users.domain.UserEventObserver;

public class UsersManagerVerticle extends AbstractVerticle implements UserEventObserver {
    private int port;
    private UsersManagerAPI usersAPI;
    private static final String USER_MANAGER_EVENTS = "users-manager-events";

    public UsersManagerVerticle(int port, UsersManagerAPI usersAPI) {
        this.port = port;
        this.usersAPI = usersAPI;
    }

    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        router.route(HttpMethod.GET, "/api/users").handler(this::getAllUsers);
        router.route(HttpMethod.POST, "/api/users").handler(this::createUser);
        router.route(HttpMethod.GET, "/api/users/:userId").handler(this::getUserByID);
        router.route(HttpMethod.POST, "/api/users/:userId/recharge-credit").handler(this::rechargeCredit);
        router.route(HttpMethod.POST, "/api/users/:userId/decrease-credit").handler(this::decreaseCredit);
        router.route("/api/users/events").handler(this::handleEventSubscription);
        router.route("/api/users/:userId/events/").handler(this::handleEventSubscription);
        
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

    protected void getAllUsers(RoutingContext context) {
        JsonObject response = new JsonObject();
        try {
            response.put("users", this.usersAPI.getAllUsers());
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void createUser(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String userID = data.getString("userId");
            JsonObject response = new JsonObject();
            try {
                response.put("user", this.usersAPI.createUser(userID));
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void getUserByID(RoutingContext context) {
        String userID = context.pathParam("userId");
        JsonObject response = new JsonObject();
        try {
            var userOpt = this.usersAPI.getUserByID(userID);
            if (userOpt.isPresent()) {
                response.put("user", userOpt.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void rechargeCredit(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String userID = context.pathParam("userId");
            int credit;
            try {
                credit = Integer.parseInt(data.getString("credit"));
            } catch (Exception ex) {
                sendBadRequest(context.response(), ex);
                return;
            }

            JsonObject response = new JsonObject();
            try {
                this.usersAPI.rechargeCredit(userID, credit);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void decreaseCredit(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String userID = context.pathParam("userId");
            int credit;
            try {
                credit = Integer.parseInt(data.getString("credit"));
            } catch (Exception ex) {
                sendBadRequest(context.response(), ex);
                return;
            }

            JsonObject response = new JsonObject();
            try {
                this.usersAPI.decreaseCredit(userID, credit);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void handleEventSubscription(RoutingContext context){
        Optional<String> userID = Optional.of(context.pathParam("userId"));
        HttpServerRequest request = context.request();
        var wsFuture = request.toWebSocket();
        wsFuture.onSuccess(webSocket -> {
            JsonObject reply = new JsonObject();
            
            if(userID.isEmpty()){
                JsonArray users = this.usersAPI.getAllUsers();
                reply.put("users", users);
                webSocket.accept();
            } else{
                Optional<JsonObject> user = this.usersAPI.getUserByID(userID.get());
                if (user.isPresent()){
                    reply.put("user", user.get());
                    webSocket.accept();
                } else{
                    webSocket.reject();
                    return;
                }
            }

            reply.put("event", "subscription-started");
            webSocket.writeTextMessage(reply.encodePrettily());
            var eventBus = vertx.eventBus();
            var consumer = eventBus.consumer(USER_MANAGER_EVENTS, msg -> {
                JsonObject user = (JsonObject) msg.body();
                if(userID.isEmpty() || userID.get().equals(user.getString("userId"))){
                    webSocket.writeTextMessage(user.encodePrettily());
                }
            });

            webSocket.textMessageHandler(data -> {
                JsonObject obj = new JsonObject(data);
                if(obj.containsKey("unsubscribe")){
                    consumer.unregister();
                    webSocket.close();
                }
            });
        });
    }

    @Override
    public void userUpdated(String userID, int credit) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", "user-update")
            .put("userId", userID)
            .put("credit", credit);
        eventBus.publish(USER_MANAGER_EVENTS, obj);
    }
}