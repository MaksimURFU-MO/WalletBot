package bot;

import model.Currency;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record RateOnDateRequest(Currency currency, LocalDate date) {

    private static final Map<String, Currency> CURRENCY_ALIASES = Map.ofEntries(
            Map.entry("доллар", Currency.USD),
            Map.entry("доллара", Currency.USD),
            Map.entry("доллары", Currency.USD),
            Map.entry("евро", Currency.EUR),
            Map.entry("юань", Currency.CNY),
            Map.entry("юаня", Currency.CNY),
            Map.entry("фунт", Currency.GBP),
            Map.entry("фунта", Currency.GBP),
            Map.entry("иена", Currency.JPY),
            Map.entry("иены", Currency.JPY),
            Map.entry("йена", Currency.JPY),
            Map.entry("йены", Currency.JPY),
            Map.entry("франк", Currency.CHF),
            Map.entry("франка", Currency.CHF),
            Map.entry("лира", Currency.TRY),
            Map.entry("лиры", Currency.TRY),
            Map.entry("тенге", Currency.KZT),
            Map.entry("рубль", Currency.RUB),
            Map.entry("рубля", Currency.RUB)
    );

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("января", 1), Map.entry("январь", 1),
            Map.entry("февраля", 2), Map.entry("февраль", 2),
            Map.entry("марта", 3), Map.entry("март", 3),
            Map.entry("апреля", 4), Map.entry("апрель", 4),
            Map.entry("мая", 5), Map.entry("май", 5),
            Map.entry("июня", 6), Map.entry("июнь", 6),
            Map.entry("июля", 7), Map.entry("июль", 7),
            Map.entry("августа", 8), Map.entry("август", 8),
            Map.entry("сентября", 9), Map.entry("сентябрь", 9),
            Map.entry("октября", 10), Map.entry("октябрь", 10),
            Map.entry("ноября", 11), Map.entry("ноябрь", 11),
            Map.entry("декабря", 12), Map.entry("декабрь", 12)
    );

    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    public static Optional<RateOnDateRequest> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("курс ")) {
            return Optional.empty();
        }

        String rest = normalized.substring("курс ".length()).trim();
        String[] parts = rest.split("\\s+");
        if (parts.length < 2) {
            return Optional.empty();
        }

        Optional<Currency> currency = parseCurrency(parts[0]);
        if (currency.isEmpty()) {
            return Optional.empty();
        }

        int dateStartIdx = (parts.length > 2 && parts[1].equals("на")) ? 2 : 1;
        Optional<LocalDate> date = parseDate(parts, dateStartIdx);
        if (date.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new RateOnDateRequest(currency.get(), date.get()));
    }

    private static Optional<Currency> parseCurrency(String token) {
        Optional<Currency> byCode = Currency.fromCode(token);
        if (byCode.isPresent()) {
            return byCode;
        }
        return Optional.ofNullable(CURRENCY_ALIASES.get(token));
    }

    private static Optional<LocalDate> parseDate(String[] parts, int startIdx) {
        int remaining = parts.length - startIdx;
        if (remaining == 1) {
            String single = parts[startIdx];
            try {
                return Optional.of(LocalDate.parse(single, DOT_DATE));
            } catch (DateTimeParseException ignored) {
            }
            try {
                return Optional.of(LocalDate.parse(single, DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException ignored) {
            }
            return Optional.empty();
        }

        if (remaining >= 3) {
            try {
                int day = Integer.parseInt(parts[startIdx]);
                Integer month = MONTHS.get(parts[startIdx + 1]);
                String yearToken = parts[startIdx + 2].replaceAll("[^0-9]", "");
                if (month == null || yearToken.length() != 4) {
                    return Optional.empty();
                }
                int year = Integer.parseInt(yearToken);
                return Optional.of(LocalDate.of(year, month, day));
            } catch (NumberFormatException | DateTimeException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}