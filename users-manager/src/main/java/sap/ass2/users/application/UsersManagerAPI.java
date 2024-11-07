package sap.ass2.users.application;

import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.users.domain.RepositoryException;
import sap.ass2.users.domain.UserEventObserver;

public interface UsersManagerAPI {
    /**
     * Retrieves all users.
     * @return a list of users
     */
    JsonArray getAllUsers();

    /**
     * Creates a new user.
     * @param userID the user ID
     * @return the JSON object that represents the new user
     * @throws RepositoryException 
     */
    JsonObject createUser(String userID) throws RepositoryException;

    /**
     * Retrieves a user by its ID.
     * @param userID the user ID
     * @return the JSON object that represents the user if it exists
     */
    Optional<JsonObject> getUserByID(String userID);

    /**
     * Recharges a user's credit.
     * @param userID the user ID
     * @param credit the amount of credit to add
     * @throws IllegalArgumentException 
     * @throws RepositoryException 
     */
    void rechargeCredit(String userID, int credit) throws RepositoryException, IllegalArgumentException;

    /**
     * Decreases a user's credit.
     * @param userID the user ID
     * @param amount the amount of credit to subtract
     * @throws RepositoryException 
     */
    void decreaseCredit(String userID, int amount) throws RepositoryException;

    /**
     * Subscribes an observer for all user events.
     * @param observer the observer
     */
    void subscribeForUserEvents(UserEventObserver observer);

    /**
     * Subscribes an observer for events regarding a specific user.
     * @param userID the user ID
     * @param observer the observer
     */
    void subscribeForUserEvents(String userID, UserEventObserver observer);

    /**
     * Unsubscribes an observer for events regarding a specific user.
     * @param userID the user ID
     * @param observer the observer
     */
    void unsubscribeForUserEvents(String userID, UserEventObserver observer);
}
