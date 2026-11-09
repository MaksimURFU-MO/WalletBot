import bot.ConsoleBot;
import bot.RateMessageHandler;
import client.CbrApiClient;
import client.CbrJsonParser;
import client.ParsedRateSource;
import client.RateSource;
import client.RatesParser;
import client.RawDataSource;
import service.CbrCurrencyService;
import service.CurrencyService;

public class ConsoleMain {
    public static void main(String[] args) {
        RawDataSource apiClient = new CbrApiClient();
        RatesParser parser = new CbrJsonParser();
        RateSource rateSource = new ParsedRateSource(apiClient, parser);
        CurrencyService service = new CbrCurrencyService(rateSource);

        new ConsoleBot(new RateMessageHandler(service)).run();
    }
}