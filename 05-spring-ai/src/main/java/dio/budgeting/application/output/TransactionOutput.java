package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;

public record TransactionOutput(String id, String description, String category, double value) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                centsToReais(transaction.getAmount()));
    }

    // amount é armazenado em centavos; a saída é em reais (ex.: 1050 -> 10.50)
    public static double centsToReais(long cents) {
        return BigDecimal.valueOf(cents, 2).doubleValue();
    }
}
