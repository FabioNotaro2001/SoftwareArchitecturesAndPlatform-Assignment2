package sap.ass2.ebikes.infrastructure;

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
import sap.ass2.ebikes.application.EbikesManagerAPI;
import sap.ass2.ebikes.domain.Ebike.EbikeState;
import sap.ass2.ebikes.domain.EbikeEventObserver;

public class EbikesManagerVerticle extends AbstractVerticle implements EbikeEventObserver{
    private int port;
    private EbikesManagerAPI ebikesAPI;
    private static final String EBIKES_MANAGER_EVENTS = "ebikes-manager-events";
    
    private static final String UPDATE_EVENT = "ebike-update";
    private static final String REMOVE_EVENT = "ebike-remove";
    
    public EbikesManagerVerticle(int port, EbikesManagerAPI usersAPI) {
        this.port = port;
        this.ebikesAPI = usersAPI;
    }

    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        router.route(HttpMethod.GET, "/api/ebikes").handler(this::getAllEbikes);
        router.route(HttpMethod.GET, "/api/ebikes/ids").handler(this::getAllAvailableEbikesIds);
        router.route(HttpMethod.POST, "/api/ebikes").handler(this::createEbike);
        router.route(HttpMethod.GET, "/api/ebikes/:ebikeId").handler(this::getEbikeByID);
        router.route(HttpMethod.DELETE, "/api/ebikes/:ebikeId").handler(this::deleteEbike);
        router.route(HttpMethod.POST, "/api/ebikes/:ebikeId").handler(this::updateEbike);
        router.route("/api/ebikes/events").handler(this::handleEventSubscription);
        router.route("/api/ebikes/:ebikeId/events").handler(this::handleEventSubscription);
        
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

    protected void getAllEbikes(RoutingContext context) {
        JsonObject response = new JsonObject();
        try {
            response.put("ebikes", this.ebikesAPI.getAllEbikes());
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void getAllAvailableEbikesIds(RoutingContext context) {
        JsonObject response = new JsonObject();
        try {
            response.put("ebikes", this.ebikesAPI.getAllAvailableEbikesIDs());
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void createEbike(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String ebikeID = data.getString("ebikeId");
            double x = data.getDouble("x");
            double y = data.getDouble("y");
            
            JsonObject response = new JsonObject();
            try {
                response.put("ebike", this.ebikesAPI.createEbike(ebikeID, x, y));
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void deleteEbike(RoutingContext context) {
        JsonObject response = new JsonObject();
        String ebikeID = context.request().getParam("ebikeId");
        try {
            this.ebikesAPI.removeEbike(ebikeID);
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void getEbikeByID(RoutingContext context) {
        String ebikeID = context.pathParam("ebikeId");
        JsonObject response = new JsonObject();
        try {
            var ebikeOpt = this.ebikesAPI.getEbikeByID(ebikeID);
            if (ebikeOpt.isPresent()) {
                response.put("ebike", ebikeOpt.get());
            }
            sendReply(context.response(), response);
        } catch (Exception ex) {
            sendServiceError(context.response(), ex);
        }
    }

    protected void updateEbike(RoutingContext context) {
        context.request().handler(buffer -> {
            JsonObject data = buffer.toJsonObject();
            String ebikeID = context.pathParam("ebikeId");
            Optional<EbikeState> state;
            Optional<Double> x, y, dirX, dirY, speed;
            Optional<Integer> batteryLevel;
            try {
                state = Optional.of(data.getString("state")).map(EbikeState::valueOf);
                x = Optional.of(data.getDouble("x"));
                y = Optional.of(data.getDouble("y"));
                dirX = Optional.of(data.getDouble("dirX"));
                dirY = Optional.of(data.getDouble("dirY"));
                speed = Optional.of(data.getDouble("speed"));
                batteryLevel = Optional.of(data.getInteger("batteryLevel"));
            } catch (Exception ex) {
                sendBadRequest(context.response(), ex);
                return;
            }

            JsonObject response = new JsonObject();
            try {
                this.ebikesAPI.updateEbike(ebikeID, state, x, y, dirX, dirY, speed, batteryLevel);
                sendReply(context.response(), response);
            } catch (Exception ex) {
                sendServiceError(context.response(), ex);
            }
        });
    }

    protected void handleEventSubscription(RoutingContext context){
        Optional<String> ebikeID = Optional.of(context.pathParam("ebikeId"));
        HttpServerRequest request = context.request();
        var wsFuture = request.toWebSocket();
        wsFuture.onSuccess(webSocket -> {
            JsonObject reply = new JsonObject();
            
            if(ebikeID.isEmpty()){
                JsonArray ebikes = this.ebikesAPI.getAllEbikes();
                reply.put("ebikes", ebikes);
                webSocket.accept();
            } else{
                Optional<JsonObject> ebike = this.ebikesAPI.getEbikeByID(ebikeID.get());
                if (ebike.isPresent()){
                    reply.put("ebike", ebike.get());
                    webSocket.accept();
                } else{
                    webSocket.reject();
                    return;
                }
            }

            reply.put("event", "subscription-started");
            webSocket.writeTextMessage(reply.encodePrettily());
            var eventBus = vertx.eventBus();
            var consumer = eventBus.consumer(EBIKES_MANAGER_EVENTS, msg -> {
                JsonObject ebike = (JsonObject) msg.body();
                if(ebikeID.isEmpty() || ebikeID.get().equals(ebike.getString("ebikeId"))){
                    webSocket.writeTextMessage(ebike.encodePrettily());
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
    public void ebikeUpdated(String ebikeID, EbikeState state, 
                            double locationX, double locationY, double directionX, double directionY, double speed, 
                            int batteryLevel) {
            var eventBus = vertx.eventBus();
            var obj = new JsonObject()
                .put("event", UPDATE_EVENT)
                .put("ebikeId", ebikeID)
                .put("state", state.toString())
                .put("x", locationX)
                .put("y", locationY)
                .put("dirX", directionX)
                .put("dirY", directionY)
                .put("speed", speed)
                .put("batteryLevel", batteryLevel);
            eventBus.publish(EBIKES_MANAGER_EVENTS, obj);
    }

    @Override
    public void ebikeRemoved(String ebikeID) {
        var eventBus = vertx.eventBus();
        var obj = new JsonObject()
            .put("event", REMOVE_EVENT)
            .put("ebikeId", ebikeID);
        eventBus.publish(EBIKES_MANAGER_EVENTS, obj);
    }
}