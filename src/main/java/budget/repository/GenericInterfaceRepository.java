package budget.repository;
import java.util.ArrayList;

/**
 * Generic interface for basic CRUD operations
 */
public interface GenericInterfaceRepository<T, ID> {
    /** Loads all data */
    ArrayList<T> load();
    
    /** Saves an entity */
    void save(T entity); 
    /** Checks if an entity exists by ID */
    boolean exist(ID id);
}
