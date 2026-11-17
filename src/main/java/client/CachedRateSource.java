package client;

import model.Currency;
import model.Rate;
import service.CurrencyException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedRateSource implements RateSource {

    private record CacheEntry(Map<Currency, Rate> rates, Instant loadedAt) {
    }

    private final RateSource delegate;
    private final Duration ttl;
    private final Map<LocalDate, CacheEntry> cache = new ConcurrentHashMap<>();

    public CachedRateSource(RateSource delegate, Duration ttl) {
        this.delegate = delegate;
        this.ttl = ttl;
    }

    @Override
    public synchronized Map<Currency, Rate> fetch(LocalDate date) throws CurrencyException {
        LocalDate key = (date == null) ? LocalDate.now() : date;
        Instant now = Instant.now();
        CacheEntry entry = cache.get(key);

        if (entry == null || entry.loadedAt().plus(ttl).isBefore(now)) {
            Map<Currency, Rate> fresh = delegate.fetch(key);
            entry = new CacheEntry(fresh, now);
            cache.put(key, entry);
        }
        return entry.rates();
    }
}