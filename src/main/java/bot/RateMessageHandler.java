

package bot;

import model.Currency;
import model.Rate;
import service.CurrencyException;
import service.CurrencyService;

import java.util.Optional;

public class RateMessageHandler {

    private final CurrencyService currencyService;

    public RateMessageHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public String handle(String text) {
        String input = text == null ? "" : text.trim();

        Optional<Currency> currencyOpt = Currency.fromCode(input);
        if (currencyOpt.isEmpty()) {
            return "Пожалуйста, введи корректный 3-буквенный код валюты (например, USD).";
        }

        Currency currency = currencyOpt.get();
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