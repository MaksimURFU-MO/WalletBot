package app;

import bot.RateMessageHandler;
import client.CachedRateSource;
import client.CbrApiClient;
import client.CbrJsonParser;
import client.ParsedRateSource;
import client.RateSource;
import client.RatesParser;
import client.RawDataSource;
import service.CbrCurrencyService;
import service.CurrencyService;

import java.time.Duration;

public final class AppFactory {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private AppFactory() {
    }

    public static RateMessageHandler createHandler() {
        RawDataSource raw = new CbrApiClient();
        RatesParser parser = new CbrJsonParser();
        RateSource source = new CachedRateSource(new ParsedRateSource(raw, parser), CACHE_TTL);
        CurrencyService service = new CbrCurrencyService(source);
        return new RateMessageHandler(service);
    }
}