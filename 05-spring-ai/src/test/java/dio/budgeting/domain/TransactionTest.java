package dio.budgeting.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = new Transaction("  Compra no mercado  ", 1050, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Compra no mercado");
        assertThat(transaction.getAmount()).isEqualTo(1050);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void should_reject_when_descriptionIsBlank(String description) {
        assertThatThrownBy(() -> new Transaction(description, 1000, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void should_reject_when_descriptionIsTooLong() {
        var description = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH + 1);

        assertThatThrownBy(() -> new Transaction(description, 1000, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -5000})
    void should_reject_when_amountIsNotPositive(long amount) {
        assertThatThrownBy(() -> new Transaction("Remédio", amount, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void should_reject_when_categoryIsNull() {
        assertThatThrownBy(() -> new Transaction("Gasolina", 20000, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("categoria");
    }
}
