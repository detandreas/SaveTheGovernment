package budget.repository;
import java.util.ArrayList;

public interface GenericInterfaceRepository <T, ID> {
    ArrayList<T> load(); //Φορτωση ολων των δεδομενων
    void save(T entity); 
    boolean exist(ID id);
}
