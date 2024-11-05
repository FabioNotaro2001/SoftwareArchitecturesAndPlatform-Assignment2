package sap.ass2.users.domain;

public interface UserEventObserver {
    void userUpdated(String userID, int credit);
}
