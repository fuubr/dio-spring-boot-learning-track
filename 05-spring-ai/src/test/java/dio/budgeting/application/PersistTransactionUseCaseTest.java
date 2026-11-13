package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.support.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistTransactionUseCaseTest {
    private InMemoryTransactionRepository repository;
    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        useCase = new PersistTransactionUseCase(repository);
    }

    @Test
    void should_persistAndConvertCentsToReais() {
        var output = useCase.execute(new PersistTransactionInput("Mercado", 1050, Category.GROCERIES));

        assertThat(output.value()).isEqualTo(10.50);
        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void should_notPersist_when_inputIsInvalid() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Mercado", 0, Category.GROCERIES)))
                .isInstanceOf(InvalidTransactionException.class);

        assertThat(repository.count()).isZero();
    }
}
