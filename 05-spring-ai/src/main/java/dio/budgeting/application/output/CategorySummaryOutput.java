package dio.budgeting.application.output;

public record CategorySummaryOutput(String category, int transactions, double total) {
}
