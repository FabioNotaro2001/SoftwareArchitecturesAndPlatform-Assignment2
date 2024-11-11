package sap.ass2.rides.infrastructure;

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
import sap.ass2.rides.application.RidesManagerAPI;
import sap.ass2.rides.domain.RideEventObserver;

public class RidesManagerVerticle extends AbstractVerticle implements RideEventObserver {
    private int port;
    private RidesManagerAPI ridesAPI;
    
    private static final String RIDES_MANAGER_EVENTS = "rides-manager-events";
    
    private static final String RIDE_ID_TYPE = "ride";
    private static final String USER_ID_TYPE = "user";
    private static final String EBIKE_ID_TYPE = "ebike";
    
    private static final String UPDATE_EVENT = "ride-update";
    private static final String END_EVENT = "ride-end";
    
    public RidesManagerVerticle(int port, RidesManagerAPI ridesAPI) {
        this.port = port;
        this.ridesAPI = ridesAPI;
    }

    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        router.route(HttpMethod.GET, "/api/rides").handler(this::getAllRides);
        router.route(HttpMethod.POST, "/api/rides").handler(this::beginRide);
        router.route(HttpMethod.DELETE, "/api/rides").handler(this::stopRide);
        router.route(HttpMethod.GET, "/api/rides/:rideId").handler(this::getRideByID);
        router.route("/api/rides/events").handler(this::handleEventSubscription);
        
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

    protected void getAllRides(RoutingContext context) {
        JsonObject response = new JsonObject();
        try {
            response.put("rides", this.ridesAPI.getAllRides());
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void beginRide(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String userID = data.getString("userId");
            String ebikeID = data.getString("ebikeId");
            JsonObject response = new JsonObject();
            try {
                response.put("ride", this.ridesAPI.beginRide(userID, ebikeID));
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void stopRide(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String rideID = data.getString("rideId");
            String userID = data.getString("userId");
            JsonObject response = new JsonObject();
            try {
                this.ridesAPI.stopRide(rideID, userID);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void getRideByID(RoutingContext context) {
        String idType = context.pathParam("IdType");
        String id = context.pathParam("id");
        JsonObject response = new JsonObject();
        try {
            Optional<JsonObject> ride = Optional.empty();
            switch (idType) {
                case RIDE_ID_TYPE: {
                    ride = this.ridesAPI.getRideByRideID(id);
                    break;
                }
                case USER_ID_TYPE: {
                    ride = this.ridesAPI.getRideByUserID(id);
                    break;
                }
                case EBIKE_ID_TYPE: {
                    ride = this.ridesAPI.getRideByBikeID(id);
                    break;
                }
                default: {
                    sendBadRequest(context.response(), new IllegalArgumentException("Invalid IdType"));
                    return;
                }
            }
            if (ride.isPresent()) {
                response.put("user", ride.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void handleEventSubscription(RoutingContext context){
        HttpServerRequest request = context.request();
        var wsFuture = request.toWebSocket();
        wsFuture.onSuccess(webSocket -> {
            JsonObject reply = new JsonObject();
            
            JsonArray rides = this.ridesAPI.getAllRides();
            reply.put("rides", rides);
            webSocket.accept();

            reply.put("event", "subscription-started");
            webSocket.writeTextMessage(reply.encodePrettily());
            var eventBus = vertx.eventBus();
            eventBus.consumer(RIDES_MANAGER_EVENTS, msg -> {
                JsonObject ride = (JsonObject) msg.body();
                webSocket.writeTextMessage(ride.encodePrettily());
            });
        });
    }

    @Override
    public void rideStarted(String rideID, String userID, String ebikeID) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", UPDATE_EVENT)
            .put("rideId", rideID)
            .put("userId", userID)
            .put("ebikeId", ebikeID);
        eventBus.publish(RIDES_MANAGER_EVENTS, obj);
    }

    @Override
    public void rideEnded(String rideID) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", END_EVENT)
            .put("rideId", rideID);
        eventBus.publish(RIDES_MANAGER_EVENTS, obj);
    }

}
