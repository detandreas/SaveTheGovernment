package budget.service;

import budget.model.domain.BudgetItem;
import budget.model.domain.ChangeLog;
import budget.repository.ChangeLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service responsible for storing and retrieving {@link ChangeLog}
 * entries. This service is stateless and delegates persistence to
 * {@link ChangeLogRepository}.
 */
public class ChangeLogService {
}
