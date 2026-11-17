package client;

import model.Currency;
import model.Rate;
import org.junit.jupiter.api.Test;
import service.CurrencyException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CachedRateSourceDateTest {

    private static final LocalDate D1 = LocalDate.of(2025, 3, 7);
    private static final LocalDate D2 = LocalDate.of(2025, 3, 10);

    private final List<LocalDate> delegateCalls = new ArrayList<>();
    private CurrencyException failNextWith;

    private final RateSource delegate = date -> {
        delegateCalls.add(date);
        if (failNextWith != null) {
            CurrencyException e = failNextWith;
            failNextWith = null;
            throw e;
        }
        return Map.of(Currency.RUB, Rate.rub(date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
    };

    @Test
    void sameDateIsLoadedOnce() throws CurrencyException {
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofHours(1));

        Map<Currency, Rate> first = cache.fetch(D1);
        Map<Currency, Rate> second = cache.fetch(D1);

        assertEquals(List.of(D1), delegateCalls);
        assertSame(first, second);
    }

    @Test
    void differentDatesAreCachedSeparately() throws CurrencyException {
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofHours(1));

        Instant first = cache.fetch(D1).get(Currency.RUB).date();
        Instant second = cache.fetch(D2).get(Currency.RUB).date();
        cache.fetch(D1);
        cache.fetch(D2);

        assertEquals(List.of(D1, D2), delegateCalls);
        assertNotEquals(first, second);
    }

    @Test
    void nullDateMeansToday() throws CurrencyException {
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofHours(1));

        cache.fetch(null);
        cache.fetch(LocalDate.now());

        assertEquals(List.of(LocalDate.now()), delegateCalls);
    }

    @Test
    void fetchWithoutArgumentsMeansToday() throws CurrencyException {
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofHours(1));

        cache.fetch();

        assertEquals(List.of(LocalDate.now()), delegateCalls);
    }

    @Test
    void expiredEntryIsReloaded() throws CurrencyException {
        // отрицательный TTL: запись считается просроченной сразу
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofMillis(-1));

        cache.fetch(D1);
        cache.fetch(D1);

        assertEquals(List.of(D1, D1), delegateCalls);
    }

    @Test
    void failedLoadIsNotCached() throws CurrencyException {
        CachedRateSource cache = new CachedRateSource(delegate, Duration.ofHours(1));
        failNextWith = new CurrencyException(CurrencyException.Reason.SOURCE_UNAVAILABLE, "нет сети");

        assertThrows(CurrencyException.class, () -> cache.fetch(D1));
        Map<Currency, Rate> rates = cache.fetch(D1);

        assertNotNull(rates);
        assertEquals(List.of(D1, D1), delegateCalls);
    }
}