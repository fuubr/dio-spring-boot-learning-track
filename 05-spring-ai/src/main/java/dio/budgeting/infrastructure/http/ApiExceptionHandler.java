package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.InvalidTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Regra de negócio violada -> 400 com corpo no padrão RFC 9457 (Problem Details)
    @ExceptionHandler(InvalidTransactionException.class)
    public ProblemDetail handleInvalidTransaction(InvalidTransactionException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Transação inválida");
        return problem;
    }
}
