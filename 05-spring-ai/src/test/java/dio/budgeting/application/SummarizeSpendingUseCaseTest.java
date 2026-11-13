package dio.budgeting.application;

import dio.budgeting.application.output.CategorySummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.support.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SummarizeSpendingUseCaseTest {

    @Test
    void should_sumSpendingByCategoryAndTotal() {
        var repository = new InMemoryTransactionRepository();
        repository.save(new Transaction("Mercado", 1050, Category.GROCERIES));
        repository.save(new Transaction("Feira", 2000, Category.GROCERIES));
        repository.save(new Transaction("Farmácia", 1599, Category.PHARMA));

        var summary = new SummarizeSpendingUseCase(repository).execute();

        assertThat(summary.categories()).containsExactly(
                new CategorySummaryOutput("GROCERIES", 2, 30.50),
                new CategorySummaryOutput("PHARMA", 1, 15.99),
                new CategorySummaryOutput("AUTO", 0, 0.0));
        assertThat(summary.total()).isEqualTo(46.49);
    }

    @Test
    void should_returnZeros_when_thereAreNoTransactions() {
        var summary = new SummarizeSpendingUseCase(new InMemoryTransactionRepository()).execute();

        assertThat(summary.total()).isZero();
        assertThat(summary.categories()).allSatisfy(c -> assertThat(c.transactions()).isZero());
    }
}
