import bot.CurrencyBot;
import bot.RateMessageHandler;
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

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Собираем слой данных
            RawDataSource apiClient = new CbrApiClient();
            RatesParser parser = new CbrJsonParser();
            RateSource rateSource = new ParsedRateSource(apiClient, parser);
            CurrencyService currencyService = new CbrCurrencyService(rateSource);

            // 2. Общая логика ответов (её же использует консольный бот)
            RateMessageHandler handler = new RateMessageHandler(currencyService);

            // 3. Запускаем Telegram-бота
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CurrencyBot(handler));

            System.out.println("✅ Бот успешно запущен! Идите в Telegram.");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска: " + e.getMessage());
            e.printStackTrace();
        }
    }
}