package sap.ass2.users.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass2.users.domain.RepositoryException;
import sap.ass2.users.domain.User;
import sap.ass2.users.domain.UserEventObserver;
import sap.ass2.users.domain.UserRepository;

public class UsersManagerImpl implements UsersManagerAPI {

    private final UserRepository userRepository;
    private final List<User> users;
    private List<UserEventObserver> observers;
    private Map<UserEventObserver, String> specificUserObservers; // The string is the user id. TODO : Magari trasformare in mappa da stringa a lista di observer.

    UsersManagerImpl(UserRepository userRepository) throws RepositoryException {
        this.userRepository = userRepository;
        // FIXME: forse meglio usare strutture che gestiscono la concorrenza?
        this.observers = new ArrayList<>();
        this.specificUserObservers = new HashMap<>();
        this.users = userRepository.getUsers();
    }

    private static JsonObject toJSON(User user) {
        return new JsonObject()
            .put("id", user.getId())
            .put("credit", user.getCredit());
    }

    @Override
    public JsonArray getAllUsers() {
        return users.stream().map(UsersManagerImpl::toJSON).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    private void notifyObservers(User user) {
        this.observers.forEach(o -> o.userUpdated(user.getId(), user.getCredit()));
        this.specificUserObservers.entrySet().stream()
            .filter(e -> e.getValue().equals(user.getId()))
            .forEach(e -> e.getKey().userUpdated(user.getId(), user.getCredit()));
    }

    @Override
    public JsonObject createUser(String userID) throws RepositoryException {
        var user = new User(userID, 0);
        this.userRepository.saveUser(user);
        this.notifyObservers(user);
        return UsersManagerImpl.toJSON(user);
    }

    @Override
    public Optional<JsonObject> getUserByID(String userID) {
        var user = this.users.stream().filter(u -> u.getId().equals(userID)).findFirst();
        return user.map(UsersManagerImpl::toJSON);
    }

    @Override
    public void rechargeCredit(String userID, int credit) throws RepositoryException, IllegalArgumentException {
        var userOpt = this.users.stream().filter(u -> u.getId().equals(userID)).findFirst(); // Find user.
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid user id");
        }

        var user = userOpt.get(); // Get user.
        user.rechargeCredit(credit); // Recharge user credits.
        this.userRepository.saveUser(user); // Persist user changes.
        this.notifyObservers(user); // FIXME: Possibile incosistenza se il repo non registra l'utente.
    }

    @Override
    public void decreaseCredit(String userID, int amount) throws RepositoryException {
        var userOpt = this.users.stream().filter(u -> u.getId().equals(userID)).findFirst(); // Find user.
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid user id");
        }

        var user = userOpt.get(); // Get user.
        user.decreaseCredit(amount); // Decrease user credits.
        this.userRepository.saveUser(user); // Persist user changes.
        this.notifyObservers(user); 
    }

    @Override
    public void subscribeForUserEvents(UserEventObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void subscribeForUserEvents(String userID, UserEventObserver observer) { //FIXME: Probabilmente non la useremo, perchè non necessaria al verticle.
        this.specificUserObservers.put(observer, userID);
    }

    @Override
    public void unsubscribeForUserEvents(String userID, UserEventObserver observer) {
        this.specificUserObservers.remove(observer, userID);
    }
}
