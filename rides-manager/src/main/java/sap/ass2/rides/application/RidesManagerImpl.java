package sap.ass2.rides.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.rides.domain.Ebike;
import sap.ass2.rides.domain.Ebike.EbikeState;
import sap.ass2.rides.domain.P2d;
import sap.ass2.rides.domain.Ride;
import sap.ass2.rides.domain.RideEventObserver;
import sap.ass2.rides.domain.User;
import sap.ass2.rides.domain.V2d;

public class RidesManagerImpl implements RidesManagerAPI{
    private UsersManagerRemoteAPI userManager;
    private EbikesManagerRemoteAPI ebikemanager;
    private List<Ride> rides;
    private int nextRideId;
    private List<RideEventObserver> observers;

    public void RidesManagerAPI(UsersManagerRemoteAPI userManager, EbikesManagerRemoteAPI ebikemanager){
        this.userManager = userManager;
        this.ebikemanager = ebikemanager;
        this.rides = new ArrayList<>();
        this.nextRideId = 0;
        this.observers = new ArrayList<>();
    }

    private static JsonObject toJSON(Ride ride) {
        return new JsonObject()
            .put("rideId", ride.getId())
            .put("userId", ride.getUser().getId())
            .put("ebikeId", ride.getEbike().getId());
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
        return new Ebike(id, state, new P2d(x, y), new V2d(directionX, directionY), speed, batteryLevel);
    }

    @Override
    public JsonObject beginRide(String userID, String bikeID) throws IllegalArgumentException {
        // TODO: usare thread o verticle per eseguire la ride.
        Optional<JsonObject> user = this.userManager.getUserByID(userID).result();
        if(user.isEmpty()){
            throw new IllegalArgumentException("User " + userID + "doesn't exist!");
        }
        Optional<JsonObject> bike = this.ebikemanager.getBikeByID(bikeID).result();
        if(bike.isEmpty()){
            throw new IllegalArgumentException("Bike " + bikeID + "doesn't exist!");
        }

        Ride newRide = new Ride(String.valueOf(this.nextRideId), userFromJSON(user.get()), eBikeFromJSON(bike.get()));
        this.rides.add(newRide);
        this.nextRideId++;
        return toJSON(newRide);
    }

    @Override
    public void stopRide(String rideID, String userID) throws IllegalArgumentException {
        // TODO: thread/verticle.
        var ride = this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst();
        if(ride.isEmpty()){
            throw new IllegalArgumentException("Ride not found!");
        }
        if(!ride.get().getUser().getId().equals(userID)){
            throw new IllegalArgumentException("The current user cannot stop the specified ride!");
        }
        this.rides.remove(ride.get());
    }

    @Override
    public Optional<JsonObject> getRideByRideID(String rideID) {
        return this.rides.stream().filter(r -> r.getId().equals(rideID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public Optional<JsonObject> getRideByBikeID(String bikeID) {
        return this.rides.stream().filter(r -> r.getEbike().getId().equals(bikeID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public Optional<JsonObject> getRideByUserID(String userID) {
        return this.rides.stream().filter(r -> r.getUser().getId().equals(userID)).findFirst().map(RidesManagerImpl::toJSON);
    }

    @Override
    public void subscribeForRideEvents(RideEventObserver observer) {
        this.observers.add(observer);
    }
}

// TODO: serve l'unsubscribe??????? Non necessariamente, ma volendo potremmo far registrare la user gui e a quel punto servirebbe