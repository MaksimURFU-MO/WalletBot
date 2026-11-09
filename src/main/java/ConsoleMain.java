import bot.ConsoleBot;
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

public class ConsoleMain {
    public static void main(String[] args) {
        RawDataSource apiClient = new CbrApiClient();
        RatesParser parser = new CbrJsonParser();
        RateSource rateSource = new CachedRateSource(
                new ParsedRateSource(apiClient, parser),
                Duration.ofHours(1));
        CurrencyService service = new CbrCurrencyService(rateSource);

        new ConsoleBot(new RateMessageHandler(service)).run();
    }
}