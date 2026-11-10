import bot.CurrencyBot;
import bot.RateMessageHandler;
import client.CachedRateSource;
import client.CbrApiClient;
import client.CbrJsonParser;
import client.ParsedRateSource;
import client.RateSource;
import client.RatesParser;
import client.RawDataSource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import service.CbrCurrencyService;
import service.CurrencyService;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        try {
            RawDataSource apiClient = new CbrApiClient();
            RatesParser parser = new CbrJsonParser();
            RateSource rateSource = new CachedRateSource(
                    new ParsedRateSource(apiClient, parser),
                    Duration.ofHours(1));
            CurrencyService currencyService = new CbrCurrencyService(rateSource);
            RateMessageHandler handler = new RateMessageHandler(currencyService);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CurrencyBot(currencyService, handler));

            System.out.println("✅ Бот успешно запущен! Идите в Telegram.");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска: " + e.getMessage());
            e.printStackTrace();
        }
    }
}