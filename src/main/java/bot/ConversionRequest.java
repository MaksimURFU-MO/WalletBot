package bot;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public record ConversionRequest(BigDecimal amount, String from, String to) {

    private static final Pattern PATTERN = Pattern.compile(
            "^\\s*(\\d+(?:[.,]\\d+)?)\\s*([A-Za-z]{3})\\s*(?:->|=>|→|в|to|in)?\\s*([A-Za-z]{3})\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Пустой результат - текст не похож на запрос конвертации */
    public static Optional<ConversionRequest> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) {
            return Optional.empty();
        }
        BigDecimal amount = new BigDecimal(m.group(1).replace(',', '.'));
        return Optional.of(new ConversionRequest(
                amount,
                m.group(2).toUpperCase(Locale.ROOT),
                m.group(3).toUpperCase(Locale.ROOT)));
    }
}