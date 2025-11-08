package budget.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestPathsUtil {

    private static final List<ResourceEntry> RESOURCES = List.of(
        new ResourceEntry("budget.json", () -> PathsUtil.getBudgetInputStream()),
        new ResourceEntry("bill-ministry-map.json", () -> PathsUtil.getBillMinistryMapInputStream()),
        new ResourceEntry("users.json", () -> PathsUtil.getUsersInputStream()),
        new ResourceEntry("pending-changes.json", () -> PathsUtil.getPendingChangesInputStream()),
        new ResourceEntry("budget-changes.json", () -> PathsUtil.getBudgetChangesInputStream())
    );

    @Test
    void shouldLoadAllResourcesFromClasspath() throws IOException {
        for (ResourceEntry entry : RESOURCES) {
            try (InputStream inputStream = entry.loader().get()) {
                assertNotNull(inputStream, () -> "Δεν βρέθηκε resource: " + entry.name());
                final String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(content.length() > 0, () -> "Κενό resource: " + entry.name());
            }
        }
    }

    private record ResourceEntry(String name, ResourceSupplier loader) { }

    @FunctionalInterface
    private interface ResourceSupplier {
        InputStream get();
    }
}
