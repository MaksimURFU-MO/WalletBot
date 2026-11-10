package service;

import model.Rate;
import org.junit.jupiter.api.Test;
import service.CurrencyException.Reason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CbrCurrencyServiceLambdaTest {

    private static final Instant DATE = Instant.parse("2026-11-09T08:30:00Z");

    private static final Rate USD = new Rate("USD", "Доллар США", new BigDecimal("80.5"), 1, DATE);

    /** Сервис, у которого «ЦБ» всегда отдаёт доллар и рубль. */
    private final CbrCurrencyService service = new CbrCurrencyService(
            () -> Map.of("USD", USD, "RUB", Rate.rub(DATE)));

    @Test
    void getRateReturnsRateFromSource() throws CurrencyException {
        assertEquals(USD, service.getRate("USD"));
    }

    @Test
    void convertsUsdToRub() throws CurrencyException {
        BigDecimal result = service.convert(new BigDecimal("100"), "USD", "RUB");

        assertEquals(0, new BigDecimal("8050").compareTo(result));
    }

    @Test
    void unknownCurrencyIsReported() {
        CurrencyException e = assertThrows(CurrencyException.class, () -> service.getRate("XXX"));

        assertEquals(Reason.UNKNOWN_CURRENCY, e.getReason());
    }

    @Test
    void sourceFailureIsPassedThrough() {
        CurrencyException original = new CurrencyException(Reason.SOURCE_UNAVAILABLE, "ЦБ лежит");
        CbrCurrencyService broken = new CbrCurrencyService(() -> {
            throw original;
        });

        CurrencyException thrown = assertThrows(CurrencyException.class, () -> broken.getRate("USD"));

        assertSame(original, thrown);
    }
}