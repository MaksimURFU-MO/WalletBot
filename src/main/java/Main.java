import bot.CurrencyBot;
import client.CbrApiClient;
import client.CbrJsonParser;
import client.RateSource;
import client.RatesParser;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import service.CbrCurrencyService;
import service.CurrencyService;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Собираем слой данных
            RateSource apiClient = new CbrApiClient();
            RatesParser parser = new CbrJsonParser();
            CurrencyService currencyService = new CbrCurrencyService(apiClient, parser);

            // 2. Запускаем Telegram-бота
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CurrencyBot(currencyService));

            System.out.println("✅ Бот успешно запущен! Идите в Telegram.");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска: " + e.getMessage());
            e.printStackTrace();
        }
    }
}