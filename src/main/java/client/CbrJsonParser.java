package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Currency;
import model.Rate;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class CbrJsonParser implements RatesParser {

    @Override
    public Map<Currency, Rate> parse(String raw) throws CurrencyException {
        if (raw == null || raw.isBlank()) {
            throw new CurrencyException(Reason.BAD_DATA, "Пустой ответ источника");
        }
        try {
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            Instant date = OffsetDateTime.parse(root.get("Date").getAsString()).toInstant();
            JsonObject valutes = root.getAsJsonObject("Valute");
            if (valutes == null) {
                throw new CurrencyException(Reason.BAD_DATA, "В ответе нет поля Valute");
            }

            Map<Currency, Rate> rates = new EnumMap<>(Currency.class);
            for (Map.Entry<String, JsonElement> entry : valutes.entrySet()) {
                JsonObject v = entry.getValue().getAsJsonObject();
                String code = v.get("CharCode").getAsString().toUpperCase(Locale.ROOT);
                Currency.fromCode(code).ifPresent(currency -> rates.put(currency, new Rate(
                        code,
                        v.get("Name").getAsString(),
                        v.get("Value").getAsBigDecimal(),
                        v.get("Nominal").getAsInt(),
                        date)));
            }
            rates.put(Currency.RUB, Rate.rub(date));
            return rates;
        } catch (CurrencyException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CurrencyException(Reason.BAD_DATA, "Не удалось разобрать ответ источника", e);
        }
    }
}