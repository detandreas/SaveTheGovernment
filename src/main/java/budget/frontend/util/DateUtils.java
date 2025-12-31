package budget.frontend.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter OUTPUT_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Converts a raw ISO Date String (e.g., "2024-05-20T14:30:00")
     * into a human-readable format "dd/MM/yyyy HH:mm".
     *
     * @param rawDate the original date string to be formatted
     * @return a SimpleStringProperty containing the formatted date,
     * suitable for use in a JavaFX TableColumn
     */
    public static ObservableValue<String> formatIsoDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return new SimpleStringProperty("");
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(
                rawDate, DateTimeFormatter.ISO_DATE_TIME
            );
            return new SimpleStringProperty(dateTime.format(OUTPUT_FORMATTER));
        } catch (Exception e) {
            return new SimpleStringProperty(rawDate.replace("T", " "));
        }
    }
}