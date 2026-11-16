package client;

import model.Currency;
import model.Rate;
import service.CurrencyException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class CachedRateSource implements RateSource {

    private final RateSource delegate;
    private final Duration ttl;

    private Map<Currency, Rate> cache;
    private Instant loadedAt;

    public CachedRateSource(RateSource delegate, Duration ttl) {
        this.delegate = delegate;
        this.ttl = ttl;
    }

    @Override
    public synchronized Map<Currency, Rate> fetch() throws CurrencyException {
        Instant now = Instant.now();
        if (cache == null || loadedAt.plus(ttl).isBefore(now)) {
            cache = delegate.fetch();
            loadedAt = now;
        }
        return cache;
    }
}