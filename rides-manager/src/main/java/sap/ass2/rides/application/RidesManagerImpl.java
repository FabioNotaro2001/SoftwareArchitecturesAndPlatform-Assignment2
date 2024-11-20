package sap.ass2.rides.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.rides.domain.Ebike;
import sap.ass2.rides.domain.EbikeState;
import sap.ass2.rides.domain.Ride;
import sap.ass2.rides.domain.RideEventObserver;
import sap.ass2.rides.domain.User;
import sap.ass2.rides.infrastructure.RidesExecutionVerticle;

public class RidesManagerImpl implements RidesManagerAPI, RideEventObserver {
    private UsersManagerRemoteAPI usersManager;
    private EbikesManagerRemoteAPI ebikesManager;
    private List<Ride> rides;
    private int nextRideId;
    private List<RideEventObserver> observers;
    private RidesExecutionVerticle rideExecutor;

    public RidesManagerImpl(UsersManagerRemoteAPI usersManager, EbikesManagerRemoteAPI ebikesManager){
        this.usersManager = usersManager;
        this.ebikesManager = ebikesManager;
        this.rides = Collections.synchronizedList(new ArrayList<>());
        this.nextRideId = 0;
        this.observers = Collections.synchronizedList(new ArrayList<>());

        this.rideExecutor = new RidesExecutionVerticle(this, usersManager, ebikesManager);
        this.rideExecutor.launch();
    }

    private static JsonObject toJSON(Ride ride) {
        return new JsonObject()
            .put("rideId", ride.getId())
            .put("userId", ride.getUser().id())
            .put("ebikeId", ride.getEbike().id());
    }

    @Override
    public Future<JsonArray> getAllRides() {
        return Future.succeededFuture(this.rides.stream().map(RidesManagerImpl::toJSON).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }

    private static User userFromJSON(JsonObject obj){
        return new User(obj.getString("userId"), obj.getInteger("credit"));
    }

    private static Ebike ebikeFromJSON(JsonObject obj){
        String id = obj.getString("ebikeId");
        EbikeState state = EbikeState.valueOf(obj.getString("state"));
        double x = obj.getDouble("x");
        double y = obj.getDouble("y");
        double directionX = obj.getDouble("dirX");
        double directionY = obj.getDouble("dirY");
        double speed = obj.getDouble("speed");
        int batteryLevel = obj.getInteger("batteryLevel");
        return new Ebike(id, state, x, y, directionX, directionY, speed, batteryLevel);
    }

    private static Ebike jsonObjToEbike(JsonObject obj){
        return new Ebike(obj.getString("ebikeId"), EbikeState.valueOf(obj.getString("state")), obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("dirX"), obj.getDouble("dirY"), obj.getDouble("speed"), obj.getInteger("batteryLevel"));
    }

    @Override
    public Future<JsonObject> beginRide(String userID, String ebikeID) throws IllegalArgumentException {
        Promise<JsonObject> p = Promise.promise();
        if (rides.stream().anyMatch(r -> r.getEbike().id().equals(ebikeID))) {
            throw new IllegalArgumentException("Ebike " + ebikeID + " already in use!");
        }
        
        Future<Optional<JsonObject>> userFut = this.usersManager.getUserByID(userID);
        Future<Optional<JsonObject>> bikeFut = this.ebikesManager.getBikeByID(ebikeID);
        Future.all(userFut, bikeFut).onSuccess(cf -> {
            List<Optional<JsonObject>> results = cf.list();
            var user = results.get(0);
            var bike = results.get(1);
            if(user.isEmpty()){
                throw new IllegalArgumentException("User " + userID + " doesn't exist!");
            }
            if(bike.isEmpty()){
                throw new IllegalArgumentException("Ebike " + ebikeID + " doesn't exist!");
            }
            if(jsonObjToEbike(bike.get()).state() != EbikeState.AVAILABLE){
                throw new IllegalArgumentException("Ebike " + ebikeID + " is not available!");
            }
            this.ebikesManager.updateBike(ebikeID, Optional.of(EbikeState.IN_USE), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            
            Ride newRide = new Ride(String.valueOf(this.nextRideId), userFromJSON(user.get()), ebikeFromJSON(bike.get()));
            this.rides.add(newRide);
            this.nextRideId++;
            
            this.rideExecutor.launchRide(newRide.getId(), newRide.getUser().id(), newRide.getEbike().id());
            p.complete(toJSON(newRide));
        });
        return p.future();
    }

    @Override
    public Future<Void> stopRide(String rideID, String userID) throws IllegalArgumentException {
        var ride = this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst();
        if(ride.isEmpty()){
            throw new IllegalArgumentException("Ride not found!");
        }
        if(!ride.get().getUser().id().equals(userID)){
            throw new IllegalArgumentException("The current user cannot stop the specified ride!");
        }
        
        this.rideExecutor.stopRide(rideID); 
        return Future.succeededFuture();
    }

    @Override
    public Future<Optional<JsonObject>> getRideByRideID(String rideID) {
        return Future.succeededFuture(this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst().map(RidesManagerImpl::toJSON));
    }

    @Override
    public Future<Optional<JsonObject>> getRideByEbikeID(String bikeID) {
        return Future.succeededFuture(this.rides.stream().filter(r -> r.getEbike().id().equals(bikeID)).findFirst().map(RidesManagerImpl::toJSON));
    }

    @Override
    public Future<Optional<JsonObject>> getRideByUserID(String userID) {
        return Future.succeededFuture(this.rides.stream().filter(r -> r.getUser().id().equals(userID)).findFirst().map(RidesManagerImpl::toJSON));
    }

    @Override
    public void subscribeToRideEvents(RideEventObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void rideStarted(String rideID, String userID, String bikeID) {
        this.observers.forEach(o -> o.rideStarted(rideID, userID, bikeID));
    }

    @Override
    public void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel) {
        this.observers.forEach(o -> o.rideStep(rideID, x, y, directionX, directionY, speed, batteryLevel));
    }

    @Override
    public void rideEnded(String rideID, String reason) {
        this.observers.forEach(o -> o.rideEnded(rideID, reason));
        this.rides.removeIf(r -> r.getId().equals(rideID));
    }
}