package dio.budgeting;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.infrastructure.http.ApiExceptionHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    @Test
    void should_return400ProblemDetail_when_transactionIsInvalid() {
        var problem = new ApiExceptionHandler()
                .handleInvalidTransaction(new InvalidTransactionException("O valor da transação deve ser maior que zero."));

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Transação inválida");
        assertThat(problem.getDetail()).contains("maior que zero");
    }
}
