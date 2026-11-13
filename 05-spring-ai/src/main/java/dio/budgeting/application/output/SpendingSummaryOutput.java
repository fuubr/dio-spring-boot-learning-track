package dio.budgeting.application.output;

import java.util.List;

public record SpendingSummaryOutput(List<CategorySummaryOutput> categories, double total) {
}
