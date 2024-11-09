package sap.ass2.users.domain;

import java.util.List;
import java.util.Optional;

public interface UsersRepository {
    public void saveUser(User user) throws RepositoryException;
    public List<User> getUsers() throws RepositoryException;
    public Optional<User> getUserByID(String id) throws RepositoryException;
}
