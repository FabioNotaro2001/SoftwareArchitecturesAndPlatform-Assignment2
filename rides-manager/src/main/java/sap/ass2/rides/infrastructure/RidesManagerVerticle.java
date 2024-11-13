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
    
    private static final String START_EVENT = "ride-start";
    private static final String STEP_EVENT = "ride-step";
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
        router.route("/api/rides/:rideId/events").handler(this::handleEventSubscription);
        
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
                    ride = this.ridesAPI.getRideByEbikeID(id);
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
        Optional<String> rideID = Optional.of(context.pathParam("rideId"));

        HttpServerRequest request = context.request();
        var wsFuture = request.toWebSocket();
        wsFuture.onSuccess(webSocket -> {
            JsonObject reply = new JsonObject();
            if (rideID.isEmpty()) {
                JsonArray rides = this.ridesAPI.getAllRides();
                reply.put("rides", rides);
                webSocket.accept();
            } else {
                Optional<JsonObject> ride = this.ridesAPI.getRideByRideID(rideID.get());
                if (ride.isPresent()){
                    reply.put("ride", ride.get());
                    webSocket.accept();
                } else{
                    webSocket.reject();
                    return;
                }
            }

            reply.put("event", "subscription-started");
            webSocket.writeTextMessage(reply.encodePrettily());
            var eventBus = vertx.eventBus();
            var consumer = eventBus.consumer(RIDES_MANAGER_EVENTS, msg -> {
                JsonObject ride = (JsonObject) msg.body();
                if(rideID.isEmpty() || rideID.get().equals(ride.getString("rideId"))){
                    webSocket.writeTextMessage(ride.encodePrettily());
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
    public void rideStarted(String rideID, String userID, String ebikeID) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", START_EVENT)
            .put("rideId", rideID)
            .put("userId", userID)
            .put("ebikeId", ebikeID);
        eventBus.publish(RIDES_MANAGER_EVENTS, obj);
    }

    @Override
    public void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel) {   // TODO: invocato (oltre agli altri) nel thread/verticle della ride specifica
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", STEP_EVENT)
            .put("rideId", rideID)
            .put("x", x)
            .put("y", y)
            .put("dirX", directionX)
            .put("dirY", directionY)
            .put("speed", speed)
            .put("batteryLevel", batteryLevel);
        eventBus.publish(RIDES_MANAGER_EVENTS, obj);
    }

    @Override
    public void rideEnded(String rideID, String reason) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", END_EVENT)
            .put("rideId", rideID)
            .put("reason", reason);
        eventBus.publish(RIDES_MANAGER_EVENTS, obj);
    }

}
