package com.luizalabs.orders.common.util;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FixedWidthParser - Testes Unitários")
class FixedWidthParserTest {

    private static final String VALID_LINE =
            "0000000070                              Palmer"
                    + " Prosacco0000000753000000000300000018367420210308";

    @Test
    @DisplayName("Deve validar linha com tamanho correto")
    void shouldValidateLineWithCorrectLength() {
        assertThatCode(() -> FixedWidthParser.validateLineLength(VALID_LINE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar exceção para linha vazia")
    void shouldThrowExceptionForEmptyLine() {
        assertThatThrownBy(() -> FixedWidthParser.validateLineLength(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamanho inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para linha curta")
    void shouldThrowExceptionForShortLine() {
        String shortLine = "abc";
        assertThatThrownBy(() -> FixedWidthParser.validateLineLength(shortLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamanho inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para linha longa")
    void shouldThrowExceptionForLongLine() {
        String longLine = "a".repeat(96);
        assertThatThrownBy(() -> FixedWidthParser.validateLineLength(longLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamanho inválido");
    }

    @Test
    @DisplayName("Deve extrair userId corretamente")
    void shouldExtractUserId() {
        Long userId = FixedWidthParser.extractUserId(VALID_LINE);
        assertThat(userId).isEqualTo(70L);
    }

    @Test
    @DisplayName("Deve extrair userName corretamente")
    void shouldExtractUserName() {
        String userName = FixedWidthParser.extractUserName(VALID_LINE);
        assertThat(userName).isEqualTo("Palmer Prosacco");
    }

    @Test
    @DisplayName("Deve extrair orderId corretamente")
    void shouldExtractOrderId() {
        Long orderId = FixedWidthParser.extractOrderId(VALID_LINE);
        assertThat(orderId).isEqualTo(753L);
    }

    @Test
    @DisplayName("Deve extrair productId corretamente")
    void shouldExtractProductId() {
        Long productId = FixedWidthParser.extractProductId(VALID_LINE);
        assertThat(productId).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve extrair value corretamente")
    void shouldExtractValue() {
        BigDecimal value = FixedWidthParser.extractValue(VALID_LINE);
        assertThat(value).isEqualByComparingTo("183674.00");
    }

    @Test
    @DisplayName("Deve extrair date corretamente")
    void shouldExtractDate() {
        LocalDate date = FixedWidthParser.extractDate(VALID_LINE);
        assertThat(date).isEqualTo(LocalDate.of(2021, 3, 8));
    }

    @Test
    @DisplayName("Deve lançar exceção para userId inválido")
    void shouldThrowExceptionForInvalidUserId() {
        String invalidLine = "ABCDEFGHIJ" + " ".repeat(85);

        assertThatThrownBy(() -> FixedWidthParser.extractUserId(invalidLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("user_id");
    }

    @Test
    @DisplayName("Deve lançar exceção para date inválida")
    void shouldThrowExceptionForInvalidDate() {
        String invalidLine = VALID_LINE.substring(0, 87) + "99999999";

        assertThatThrownBy(() -> FixedWidthParser.extractDate(invalidLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    @DisplayName("Deve lançar exceção para campo fora dos limites")
    void shouldThrowExceptionForOutOfBounds() {
        String shortLine = "0000000001";

        assertThatThrownBy(() -> FixedWidthParser.extractUserName(shortLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve extrair value com centavos zero")
    void shouldExtractValueWithZeroCents() {
        String line = VALID_LINE.substring(0, 75) + "000000001234" + VALID_LINE.substring(87);
        BigDecimal value = FixedWidthParser.extractValue(line);
        assertThat(value).isEqualByComparingTo("1234.00");
    }
}
