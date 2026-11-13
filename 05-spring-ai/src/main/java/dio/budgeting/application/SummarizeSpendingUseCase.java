package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SummarizeSpendingUseCase {
    private final TransactionRepository transactionRepository;

    public SummarizeSpendingUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-spending",
            description = "Retorna o total gasto (em reais) e a quantidade de transações por categoria, além do total geral")
    public SpendingSummaryOutput execute() {
        var categories = Arrays.stream(Category.values())
                .map(this::summarize)
                .toList();

        long totalCents = Arrays.stream(Category.values())
                .flatMap(c -> transactionRepository.findAllByCategory(c).stream())
                .mapToLong(Transaction::getAmount)
                .sum();

        return new SpendingSummaryOutput(categories, TransactionOutput.centsToReais(totalCents));
    }

    private CategorySummaryOutput summarize(Category category) {
        var transactions = transactionRepository.findAllByCategory(category);
        long cents = transactions.stream().mapToLong(Transaction::getAmount).sum();
        return new CategorySummaryOutput(category.name(), transactions.size(), TransactionOutput.centsToReais(cents));
    }
}
