package sap.ass2.ebikes.domain;

import java.util.List;
import java.util.Optional;

public interface EbikesRepository {
    /**
     * Saves an ebike object to the persistence layer.
     * @param eBike The ebike object to be saved.
     * @throws RepositoryException if an error occurs during the save operation.
     */
    void saveEbike(Ebike eBike) throws RepositoryException;

    /**
     * Retrieves a list of all ebikes from the persistence layer.
     * @return A list of EBike objects.
     * @throws RepositoryException if an error occurs during the retrieval operation.
     */
    List<Ebike> getEbikes() throws RepositoryException; 

    /**
     * Retrieves an ebike by its unique identifier.
     * @param id The unique identifier of the ebike to be retrieved.
     * @return An Optional containing the EBike object if found; otherwise, empty.
     * @throws RepositoryException if an error occurs during the retrieval operation.
     */
    Optional<Ebike> getEbikeByID(String id) throws RepositoryException; 
}
