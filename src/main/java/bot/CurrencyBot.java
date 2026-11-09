package bot;

import model.Rate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.CurrencyException;
import service.CurrencyService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class CurrencyBot extends TelegramLongPollingBot {

    private final ConversionFormatter formatter = new ConversionFormatter();
    private final CurrencyService currencyService;
    private final RateMessageHandler handler;

    public CurrencyBot(CurrencyService currencyService, RateMessageHandler handler) {
        this.currencyService = currencyService;
        this.handler = handler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (messageText.equalsIgnoreCase("/start")) {
                sendStartMessage(chatId);
                return;
            }

            // 1. Сначала проверяем, является ли сообщение запросом на конвертацию (например, "100 USD RUB")
            Optional<ConversionRequest> requestOpt = ConversionRequest.parse(messageText);

            if (requestOpt.isPresent()) {
                handleConversion(chatId, requestOpt.get());
            }
            // 2. Если это не конвертация, проверяем, вдруг это просто 3-буквенный код (например, "USD")
            else if (messageText.length() == 3) {
                handleSingleCurrencyInfo(chatId, messageText.toUpperCase());
            }
            // 3. Если ничего не подошло
            else {
                sendMessage(chatId, "Не понял команду. Введи код валюты (например, USD) или запрос на конвертацию (например, 100 USD RUB).");
            }
        }
    }

    // --- Логика конвертации ---
    private void handleConversion(long chatId, ConversionRequest req) {
        try {
            BigDecimal result = currencyService.convert(req.amount(), req.from(), req.to());
            Rate toRate = currencyService.getRate(req.to());

            String reply = formatter.conversion(
                    req.amount(),
                    req.from(),
                    req.to(),
                    result,
                    toRate.date()
            );
            sendMessage(chatId, reply);

        } catch (CurrencyException e) {
            if (e.getReason() == CurrencyException.Reason.UNKNOWN_CURRENCY) {
                sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
            } else {
                sendMessage(chatId, "❌ Ошибка сервиса: " + e.getMessage());
            }
        }
    }

    // --- Логика показа курса одной валюты (как было до этого) ---
    private void handleSingleCurrencyInfo(long chatId, String currencyCode) {
        try {
            Rate rate = currencyService.getRate(currencyCode);
            String response = String.format("Курс %s (%s): %s руб. за %d ед.",
                    rate.code(), rate.name(), rate.value().toPlainString(), rate.nominal());
            sendMessage(chatId, response);
        } catch (CurrencyException e) {
            if (e.getReason() == CurrencyException.Reason.UNKNOWN_CURRENCY) {
                sendMessage(chatId, "Валюта " + currencyCode + " не найдена.");
            } else {
                sendMessage(chatId, "Ошибка получения данных из сети: " + e.getMessage());
            }
        }
    }

    private void sendStartMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Привет! Я бот для курсов валют. Выбери валюту ниже или напиши запрос, например:\n100 USD RUB\n50 EUR в CNY");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("USD");
        row.add("EUR");
        row.add("CNY");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getConfigValue(String key) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("Token.properties")) {
            if (input == null) {
                System.err.println("Ошибка: файл Token.properties не найден в папке resources!");
                return "";
            }
            properties.load(input);
            return properties.getProperty(key);
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении Token.properties: " + ex.getMessage());
            return "";
        }
    }

    @Override
    public String getBotUsername() {
        return getConfigValue("bot.username");
    }

    @Override
    public String getBotToken() {
        return getConfigValue("bot.token");
    }
}