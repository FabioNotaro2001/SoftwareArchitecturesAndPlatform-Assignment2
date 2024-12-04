package sap.ass2.ebikes.domain;

import java.util.List;
import java.util.Optional;

/** Describes the operations that the ebikes service can do with its repository. */
public interface EbikesRepository {
    void saveEbike(Ebike eBike) throws RepositoryException;

    List<Ebike> getEbikes() throws RepositoryException; 

    Optional<Ebike> getEbikeByID(String id) throws RepositoryException; 
}
