package sap.ass2.ebikes.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.ebikes.domain.EbikeEventObserver;
import sap.ass2.ebikes.domain.RepositoryException;
import sap.ass2.ebikes.domain.Ebike.EbikeState;

public interface EbikesManagerAPI {
    /**
     * Retrieves all ebikes.
     * @return a list of ebikes
     */
    JsonArray getAllEbikes();

    /**
     * Retrieves all ebike ids.
     * @return a list of ebike ids.
     */
    JsonArray getAllAvailableEbikesIDs();

    /**
     * Creates a new ebike.
     * @param ebikeID the bike's ID
     * @param locationX the ebike's x coordinate
     * @param locationY the ebike's y coordinate
     * @return the newly created ebike
     * @throws RepositoryException if there's an error while performing the action
     * @throws IllegalArgumentException if the ebike's ID is already in use
     */
    JsonObject createEbike(String ebikeID, double locationX, double locationY) throws RepositoryException, IllegalArgumentException;

    /**
     * Removes an ebike.
     * @param ebikeID the ebike's ID
     * @throws RepositoryException if there's an error while performing the action
     * @throws IllegalArgumentException if the ebike does not exist
     * @throws IllegalStateException if the ebike is currently in use
     */
    void removeEbike(String ebikeID) throws RepositoryException, IllegalArgumentException, IllegalStateException;

    /**
     * Retrieves an ebike.
     * @param ebikeID the ebike's ID
     * @return an optional containing the ebike if available, otherwise an empty optional
     */
    Optional<JsonObject> getEbikeByID(String ebikeID);

    /**
     * Updates an ebike.
     * @param ebikeID the ebike's ID
     * @param state the ebike's state
     * @param locationX the ebike's x coordinate
     * @param locationY the ebike's y coordinate
     * @param directionX the ebike's x direction
     * @param directionY the ebike's y direction
     * @param speed the ebike's speed
     * @param batteryLevel the ebike's battery level
     * @throws RepositoryException if there's an error while performing the action
     * @throws IllegalArgumentException if the ebike does not exist
     */
    void updateEbike(String ebikeID, 
                    Optional<EbikeState> state, 
                    Optional<Double> locationX, Optional<Double> locationY, 
                    Optional<Double> directionX, Optional<Double> directionY, 
                    Optional<Double> speed, 
                    Optional<Integer> batteryLevel) throws RepositoryException, IllegalArgumentException;

    /**
     * Subscribes an observer for all ebike events.
     * @param observer the observer
     */
    void subscribeForEbikeEvents(EbikeEventObserver observer);

    /**
     * Subscribes an observer for events regarding a specific ebike.
     * @param userID the ebike's ID
     * @param observer the observer
     */
    void subscribeForEbikeEvents(String ebikeID, EbikeEventObserver observer);

    /**
     * Unsubscribes an observer for events regarding a specific ebike.
     * @param userID the ebike's ID
     * @param observer the observer
     */
    void unsubscribeForEbikeEvents(String ebikeID, EbikeEventObserver observer);
}