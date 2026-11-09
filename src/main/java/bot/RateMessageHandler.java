

package bot;

import model.Rate;
import service.CurrencyException;
import service.CurrencyService;

public class RateMessageHandler {

    private final CurrencyService currencyService;

    public RateMessageHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public String handle(String text) {
        String input = text == null ? "" : text.trim();

        if (input.length() != 3) {
            return "Пожалуйста, введи 3-буквенный код валюты (например, USD).";
        }

        String code = input.toUpperCase();
        try {
            Rate rate = currencyService.getRate(code);
            return String.format("Курс %s (%s): %s руб. за %d ед.",
                    rate.code(), rate.name(), rate.value().toPlainString(), rate.nominal());
        } catch (CurrencyException e) {
            if (e.getReason() == CurrencyException.Reason.UNKNOWN_CURRENCY) {
                return "Валюта " + code + " не найдена.";
            }
            return "Ошибка получения данных из сети: " + e.getMessage();
        }
    }
}