package budget.repository;

import budget.model.domain.ChangeLog;
import budget.util.PathsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing ChangeLog records.
 * This class provides basic create,read,update,delete operations for ChangeLog objects,
 * persisting them to a JSON file using Gson.
 */
public class ChangeLogRepository {

}