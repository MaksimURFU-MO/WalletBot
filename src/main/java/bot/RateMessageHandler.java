package bot;

import model.Currency;
import model.Rate;
import service.CurrencyException;
import service.CurrencyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class RateMessageHandler {

    private static final String HELP =
            "Не понял команду. Введи код валюты (например, USD), запрос на конвертацию (например, 100 USD RUB) " +
                    "или курс на дату (например, курс евро на 1 января 2026).";

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final CurrencyService currencyService;
    private final ConversionFormatter formatter = new ConversionFormatter();

    public RateMessageHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public String handle(String text) {
        String input = text == null ? "" : text.trim();

        Optional<RateOnDateRequest> rateOnDate = RateOnDateRequest.parse(input);
        if (rateOnDate.isPresent()) {
            return handleRateOnDate(rateOnDate.get());
        }

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

    private String handleRateOnDate(RateOnDateRequest req) {
        if (req.date().isAfter(LocalDate.now())) {
            return "❌ Нельзя узнать курс на будущую дату.";
        }
        try {
            Rate rate = currencyService.getRate(req.currency(), req.date());
            return String.format("Курс %s (%s) на %s (фактическая дата ЦБ: %s): %s руб. за %d ед.",
                    rate.code(),
                    rate.name(),
                    req.date().format(DISPLAY_DATE),
                    rate.date(),
                    rate.value().toPlainString(),
                    rate.nominal());
        } catch (CurrencyException e) {
            return "❌ Ошибка: " + e.getMessage();
        }
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