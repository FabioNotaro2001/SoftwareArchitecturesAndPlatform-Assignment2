package sap.ass2.rides.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private Map<RideEventObserver, String> specificRideObservers; // The string is the ride id.

    private RidesExecutionVerticle rideExecutor;

    public RidesManagerImpl(UsersManagerRemoteAPI usersManager, EbikesManagerRemoteAPI ebikesManager){
        this.usersManager = usersManager;
        this.ebikesManager = ebikesManager;
        this.rides = new ArrayList<>();
        this.nextRideId = 0;
        this.observers = new ArrayList<>();

        this.rideExecutor = new RidesExecutionVerticle(this, usersManager, ebikesManager);
        this.rideExecutor.start();
    }

    private static JsonObject toJSON(Ride ride) {
        return new JsonObject()
            .put("rideId", ride.getId())
            .put("userId", ride.getUser().id())
            .put("ebikeId", ride.getEbike().id());
    }

    @Override
    public JsonArray getAllRides() {
        return this.rides.stream().map(RidesManagerImpl::toJSON).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    private static User userFromJSON(JsonObject obj){
        return new User(obj.getString("id"), obj.getInteger("credit"));
    }

    private static Ebike eBikeFromJSON(JsonObject obj){
        String id = obj.getString("id");
        EbikeState state = EbikeState.valueOf(obj.getString("state"));
        double x = obj.getDouble("x");
        double y = obj.getDouble("y");
        double directionX = obj.getDouble("dirX");
        double directionY = obj.getDouble("dirY");
        double speed = obj.getDouble("speed");
        int batteryLevel = obj.getInteger("batteryLevel");
        return new Ebike(id, state, x, y, directionX, directionY, speed, batteryLevel);
    }

    @Override
    public JsonObject beginRide(String userID, String ebikeID) throws IllegalArgumentException {
        if (rides.stream().anyMatch(r -> r.getEbike().id().equals(ebikeID))) {
            throw new IllegalArgumentException("Ebike " + ebikeID + "already in use!");
        }
        Optional<JsonObject> user = this.usersManager.getUserByID(userID).result();
        if(user.isEmpty()){
            throw new IllegalArgumentException("User " + userID + "doesn't exist!");
        }
        Optional<JsonObject> bike = this.ebikesManager.getBikeByID(ebikeID).result();
        if(bike.isEmpty()){
            throw new IllegalArgumentException("Ebike " + ebikeID + "doesn't exist!");
        }
        
        
        Ride newRide = new Ride(String.valueOf(this.nextRideId), userFromJSON(user.get()), eBikeFromJSON(bike.get()));
        this.rides.add(newRide);
        this.nextRideId++;

        this.rideExecutor.launchRide(newRide.getId(), newRide.getUser().id(), newRide.getEbike().id());
        return toJSON(newRide);
    }

    @Override
    public void stopRide(String rideID, String userID) throws IllegalArgumentException {
        var ride = this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst();
        if(ride.isEmpty()){
            throw new IllegalArgumentException("Ride not found!");
        }
        if(!ride.get().getUser().id().equals(userID)){
            throw new IllegalArgumentException("The current user cannot stop the specified ride!");
        }
        
        this.rideExecutor.stopRide(rideID);
    }

    @Override
    public Optional<JsonObject> getRideByRideID(String rideID) {
        return this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public Optional<JsonObject> getRideByEbikeID(String bikeID) {
        return this.rides.stream().filter(r -> r.getEbike().id().equals(bikeID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public Optional<JsonObject> getRideByUserID(String userID) {
        return this.rides.stream().filter(r -> r.getUser().id().equals(userID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public void subscribeToRideEvents(RideEventObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void subscribeToRideEvents(String rideId, RideEventObserver observer) {
        this.specificRideObservers.put(observer, rideId);
    }
    
    @Override
    public void unsubscribeFromRideEvents(String rideId, RideEventObserver observer) {
        this.specificRideObservers.remove(observer, rideId);
    }

    @Override
    public void rideStarted(String rideID, String userID, String bikeID) {
        this.observers.forEach(o -> o.rideStarted(rideID, userID, bikeID));
        this.specificRideObservers.entrySet().stream()
            .filter(e -> e.getValue().equals(rideID))
            .map(Map.Entry::getKey)
            .forEach(o -> o.rideStarted(rideID, userID, bikeID));
    }

    @Override
    public void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel) {
        this.observers.forEach(o -> o.rideStep(rideID, x, y, directionX, directionY, speed, batteryLevel));
        this.specificRideObservers.entrySet().stream()
            .filter(e -> e.getValue().equals(rideID))
            .map(Map.Entry::getKey)
            .forEach(o -> o.rideStep(rideID, x, y, directionX, directionY, speed, batteryLevel));
    }

    @Override
    public void rideEnded(String rideID, String reason) {
        this.observers.forEach(o -> o.rideEnded(rideID, reason));
        var toRemove = this.specificRideObservers.entrySet().stream() // Calls the rideEnded event and removes the observers from the map.
            .filter(e -> e.getValue().equals(rideID))
            .map(Map.Entry::getKey)
            .map(o -> {
                o.rideEnded(rideID, reason);
                return o;
            })
            .collect(Collectors.toList());

        this.rides.removeIf(r -> r.getId().equals(rideID));
        toRemove.forEach(specificRideObservers::remove);
    }
}