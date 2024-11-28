package sap.ass2.ebikes.domain;

import java.util.List;
import java.util.Optional;

public interface EbikesRepository {
    void saveEbike(Ebike eBike) throws RepositoryException;

    List<Ebike> getEbikes() throws RepositoryException; 

    Optional<Ebike> getEbikeByID(String id) throws RepositoryException; 
}
