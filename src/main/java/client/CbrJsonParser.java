package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Rate;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CbrJsonParser implements RatesParser {

    @Override
    public Map<String, Rate> parse(String raw) throws CurrencyException {
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

            Map<String, Rate> rates = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : valutes.entrySet()) {
                JsonObject v = entry.getValue().getAsJsonObject();
                String code = v.get("CharCode").getAsString().toUpperCase(Locale.ROOT);
                rates.put(code, new Rate(
                        code,
                        v.get("Name").getAsString(),
                        v.get("Value").getAsBigDecimal(),
                        v.get("Nominal").getAsInt(),
                        date));
            }
            rates.put(Rate.RUB, Rate.rub(date));
            return rates;
        } catch (CurrencyException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CurrencyException(Reason.BAD_DATA, "Не удалось разобрать ответ источника", e);
        }
    }
}