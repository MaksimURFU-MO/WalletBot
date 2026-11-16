package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RateTest {

    private static final Instant DATE = Instant.parse("2026-10-24T08:30:00Z");

    @Test
    void keepsAllFields() {
        Rate rate = new Rate("USD", "Доллар США", new BigDecimal("80.5"), 1, DATE);

        assertEquals("USD", rate.code());
        assertEquals("Доллар США", rate.name());
        assertEquals(0, new BigDecimal("80.5").compareTo(rate.value()));
        assertEquals(1, rate.nominal());
        assertEquals(DATE, rate.date());
    }

    @Test
    void rejectsNullCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate(null, "Доллар", BigDecimal.ONE, 1, DATE));
    }

    @Test
    void rejectsEmptyCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("", "Доллар", BigDecimal.ONE, 1, DATE));
    }

    @Test
    void rejectsBlankCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("   ", "Доллар", BigDecimal.ONE, 1, DATE));
    }

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("USD", "Доллар", null, 1, DATE));
    }

    @Test
    void rejectsZeroValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("USD", "Доллар", BigDecimal.ZERO, 1, DATE));
    }

    @Test
    void rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("USD", "Доллар", new BigDecimal("-1"), 1, DATE));
    }

    @Test
    void rejectsZeroNominal() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("JPY", "Иена", BigDecimal.ONE, 0, DATE));
    }

    @Test
    void rejectsNegativeNominal() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rate("JPY", "Иена", BigDecimal.ONE, -100, DATE));
    }

    @Test
    void errorMessageNamesTheBrokenCurrency() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Rate("JPY", "Иена", BigDecimal.ONE, 0, DATE));

        assertTrue(e.getMessage().contains("JPY"));
    }

    @Test
    void rubleIsSyntheticRateOfOne() {
        Rate rub = Rate.rub(DATE);

        assertEquals("RUB", rub.code());
        assertEquals(0, BigDecimal.ONE.compareTo(rub.value()));
        assertEquals(1, rub.nominal());
        assertEquals(DATE, rub.date());
    }

    @Test
    void rubleCodeConstantMatchesRubleRate() {
        assertEquals(Rate.RUB, Rate.rub(DATE).code());
    }

    @Test
    void ratesWithSameFieldsAreEqual() {
        Rate a = new Rate("USD", "Доллар", new BigDecimal("80.5"), 1, DATE);
        Rate b = new Rate("USD", "Доллар", new BigDecimal("80.5"), 1, DATE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
