package app;

import bot.RateMessageHandler;
import client.CachedRateSource;
import client.CbrApiClient;
import client.RateSource;
import service.CbrCurrencyService;
import service.CurrencyService;

import java.time.Duration;

public final class AppFactory {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private AppFactory() {
    }

    public static RateMessageHandler createHandler() {
        RateSource apiClient = new CbrApiClient();
        RateSource cachedSource = new CachedRateSource(apiClient, CACHE_TTL);
        CurrencyService service = new CbrCurrencyService(cachedSource);
        return new RateMessageHandler(service);
    }
}