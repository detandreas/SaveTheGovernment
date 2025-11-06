package budget.repository;
import java.util.ArrayList;

/**
 * Generic interface for basic CRUD operations.
 * @param <T>  the type of entity handled by the repository.
 * @param <ID> the type of the unique identifier for the entity.
 */
public interface GenericInterfaceRepository<T, ID> {
    /** Loads all data. 
     * @return an {@link ArrayList} containing all entities.
    */
    ArrayList<T> load();
    
    /** Saves an entity. 
     * @param entity the entity to be saved.
    */
    void save(T entity); 
    
    /** Checks if an entity exists by ID. 
     * @param id the identifier of the entity.
     * @return {@code true} if the entity exists; {@code false} otherwise.
    */
    boolean exist(ID id);
}
