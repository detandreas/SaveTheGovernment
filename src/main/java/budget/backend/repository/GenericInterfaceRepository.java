package budget.backend.repository;
import java.util.List;
import java.util.Optional;

/**
 * Generic interface for basic CRUD operations.
 * @param <T>  the type of entity handled by the repository.
 * @param <ID> the type of the unique identifier for the entity.
 */
public interface GenericInterfaceRepository<T, ID> {
    /** Loads all data.
     * @return an {@link List} containing all entities.
    */
    List<T> load();

    /** Saves an entity.
     * @param entity the entity to be saved.
    */
    void save(T entity);

    /** Checks if an entity exists by ID.
     * @param id the identifier of the entity.
     * @return {@code true} if the entity exists; {@code false} otherwise.
    */
    boolean existsById(ID id);

    /**
     * Deletes an entity from the repository.
     * @param entity the entity to be deleted.
     */
    void delete(T entity);

     /**
     * Finds an entity by its identifier.
     * @param id the unique identifier of the entity to search for.
     * @return an Optional containing the entity if found,
     * otherwise an empty Optional.
     */
    Optional<T> findById(ID id);
}
