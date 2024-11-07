package sap.ass2.ebikes.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.ebikes.domain.Ebike.EBikeState;
import sap.ass2.ebikes.domain.Ebike;
import sap.ass2.ebikes.domain.EbikeEventObserver;
import sap.ass2.ebikes.domain.EbikeRepository;
import sap.ass2.ebikes.domain.P2d;
import sap.ass2.ebikes.domain.RepositoryException;
import sap.ass2.ebikes.domain.V2d;

public class EbikesManagerImpl implements EbikesManagerAPI {

    private final EbikeRepository ebikeRepository;
    private final List<Ebike> ebikes;
    private List<EbikeEventObserver> observers;
    private Map<EbikeEventObserver, String> specificEbikeObservers; // The string is the ebike id. TODO : Magari trasformare in mappa da stringa a lista di observer.

    public EbikesManagerImpl(EbikeRepository repository) throws RepositoryException {
        this.ebikeRepository = repository;
        // FIXME: forse meglio usare strutture che gestiscono la concorrenza?
        this.ebikes = ebikeRepository.getEbikes();
        this.observers = new ArrayList<>();
        this.specificEbikeObservers = new HashMap<>();
    }

    private static JsonObject toJSON(Ebike ebike) {
        return new JsonObject()
            .put("id", ebike.getId())
            .put("state", ebike.getState().toString())
            .put("x", ebike.getLocation().x())
            .put("y", ebike.getLocation().y())
            .put("dirX", ebike.getDirection().x())
            .put("dirY", ebike.getDirection().y())
            .put("speed", ebike.getSpeed())
            .put("batteryLevel", ebike.getBatteryLevel());
    }

    @Override
    public JsonArray getAllBikes() {
        return ebikes.stream().map(EbikesManagerImpl::toJSON).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    private void notifyObserversAboutUpdate(Ebike ebike) {
        this.observers.forEach(o -> o.bikeUpdated(ebike.getId(), ebike.getState(), 
            ebike.getLocation().x(), ebike.getLocation().y(), 
            ebike.getDirection().x(), ebike.getDirection().y(), ebike.getSpeed(), 
            ebike.getBatteryLevel()));
        this.specificEbikeObservers.entrySet().stream()
            .filter(e -> e.getValue().equals(ebike.getId()))
            .forEach(e -> e.getKey().bikeUpdated(ebike.getId(), ebike.getState(), 
                ebike.getLocation().x(), ebike.getLocation().y(), 
                ebike.getDirection().x(), ebike.getDirection().y(), ebike.getSpeed(), 
                ebike.getBatteryLevel()));
    }
    
    private void notifyObserversAboutRemoval(String ebikeID) {
        this.observers.forEach(o -> o.bikeRemoved(ebikeID));
        this.specificEbikeObservers.entrySet().stream()
            .filter(e -> e.getValue().equals(ebikeID))
            .forEach(e -> e.getKey().bikeRemoved(ebikeID));
    }

    @Override
    public JsonArray getAllAvailableBikesIDs() {
        return ebikes.stream().filter(Ebike::isAvailable).map(Ebike::getId).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    @Override
    public JsonObject createBike(String ebikeID, double locationX, double locationY) throws RepositoryException, IllegalArgumentException {
        if (this.ebikes.stream().anyMatch(ebike -> ebike.getId().equals(ebikeID))) {  //FIXME: forse meglio usare una mappa da id ad ebike?
            throw new IllegalArgumentException("Ebike with given id already exists.");
        }

        var ebike = new Ebike(ebikeID, new P2d(locationX, locationY));
        this.ebikeRepository.saveEbike(ebike);
        this.ebikes.add(ebike);
        this.notifyObserversAboutUpdate(ebike);
        return toJSON(ebike);
    }

    @Override
    public void removeBike(String ebikeID) throws RepositoryException, IllegalArgumentException {
        var ebikeOpt = this.ebikes.stream().filter(ebike -> ebike.getId().equals(ebikeID)).findFirst();
        if (ebikeOpt.isEmpty()) { 
            throw new IllegalArgumentException("No ebike with id " + ebikeID);
        }

        var ebike = ebikeOpt.get();
        if (ebike.isInUse()) {
            throw new IllegalStateException("Unable to remove ebike " + ebikeID + ": currently in use");
        }

        ebike.updateState(EBikeState.DISMISSED);
        this.ebikeRepository.saveEbike(ebike);

        this.notifyObserversAboutRemoval(ebike.getId());

        this.ebikes.remove(ebike);
    }

    @Override
    public Optional<JsonObject> getBikeByID(String ebikeID) {
        return this.ebikes.stream().filter(ebike -> ebike.getId().equals(ebikeID)).findFirst().map(EbikesManagerImpl::toJSON);
    }

    @Override
    public void updateBike(String ebikeID, Optional<EBikeState> state, Optional<Double> locationX,
                            Optional<Double> locationY, Optional<Double> directionX, Optional<Double> directionY,
                            Optional<Double> speed, Optional<Integer> batteryLevel) throws RepositoryException, IllegalArgumentException {
        var ebikeOpt = this.ebikes.stream().filter(ebike -> ebike.getId().equals(ebikeID)).findFirst();
        if (ebikeOpt.isEmpty()) { 
            throw new IllegalArgumentException("No ebike with id " + ebikeID);
        }

        var ebike = ebikeOpt.get();

        if (state.isPresent()) {
            ebike.updateState(state.get());
        }
        if (locationX.isPresent()) {
            ebike.updateLocation(new P2d(locationX.get(), ebike.getLocation().y()));
        }
        if (locationY.isPresent()) {
            ebike.updateLocation(new P2d(ebike.getLocation().x(), locationY.get()));
        }
        if (directionX.isPresent()) {
            ebike.updateDirection(new V2d(directionX.get(), ebike.getDirection().y()));
        }
        if (directionY.isPresent()) {
            ebike.updateDirection(new V2d(ebike.getDirection().x(), directionY.get()));
        }
        if (speed.isPresent()) {
            ebike.updateSpeed(speed.get());
        }
        if (batteryLevel.isPresent()) {
            ebike.setBatteryLevel(batteryLevel.get());
        }

        this.ebikeRepository.saveEbike(ebike);

        this.notifyObserversAboutUpdate(ebike);
    }

    @Override
    public void subscribeForEbikeEvents(EbikeEventObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void subscribeForEbikeEvents(String ebikeID, EbikeEventObserver observer) {
        this.specificEbikeObservers.put(observer, ebikeID);
    }

    @Override
    public void unsubscribeForEbikeEvents(String ebikeID, EbikeEventObserver observer) {
        this.specificEbikeObservers.remove(observer, ebikeID);
    }

}
