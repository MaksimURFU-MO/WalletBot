package bot;

import model.Currency;
import model.Rate;
import service.CurrencyException;
import service.CurrencyService;

import java.math.BigDecimal;
import java.util.Optional;

public class RateMessageHandler {

    private static final String HELP =
            "Не понял команду. Введи код валюты (например, USD) или запрос на конвертацию (например, 100 USD RUB).";

    private final CurrencyService currencyService;
    private final ConversionFormatter formatter = new ConversionFormatter();

    public RateMessageHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public String handle(String text) {
        String input = text == null ? "" : text.trim();

        Optional<ConversionRequest> conversion = ConversionRequest.parse(input);
        if (conversion.isPresent()) {
            return handleConversion(conversion.get());
        }

        Optional<Currency> currency = Currency.fromCode(input);
        if (currency.isPresent()) {
            return handleSingleRate(currency.get());
        }

        return HELP;
    }

    private String handleConversion(ConversionRequest req) {
        try {
            BigDecimal result = currencyService.convert(req.amount(), req.from(), req.to());
            Rate toRate = currencyService.getRate(req.to());
            return formatter.conversion(req.amount(), req.from(), req.to(), result, toRate.date());
        } catch (CurrencyException e) {
            if (e.getReason() == CurrencyException.Reason.UNKNOWN_CURRENCY) {
                return "❌ Ошибка: " + e.getMessage();
            }
            return "❌ Ошибка сервиса: " + e.getMessage();
        }
    }

    private String handleSingleRate(Currency currency) {
        try {
            Rate rate = currencyService.getRate(currency);
            return String.format("Курс %s (%s): %s руб. за %d ед.",
                    rate.code(), rate.name(), rate.value().toPlainString(), rate.nominal());
        } catch (CurrencyException e) {
            if (e.getReason() == CurrencyException.Reason.UNKNOWN_CURRENCY) {
                return "Валюта " + currency.name() + " не найдена.";
            }
            return "Ошибка получения данных из сети: " + e.getMessage();
        }
    }
}