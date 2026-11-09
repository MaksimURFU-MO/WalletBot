package bot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Превращает числа в красивый текст для чата: пробелы между тысячами, запятая вместо точки.
 */
public class ConversionFormatter {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Moscow"));

    private static DecimalFormat format(int decimals, boolean keepTrailingZeros) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        String fraction = (keepTrailingZeros ? "0" : "#").repeat(decimals);
        DecimalFormat f = new DecimalFormat("#,##0" + (decimals > 0 ? "." + fraction : ""), symbols);
        f.setRoundingMode(RoundingMode.HALF_UP);
        return f;
    }

    /** Сумма: 2 знака, но если результат меньше единицы, показываем 4, чтобы не терять точность. */
    public String money(BigDecimal value) {
        boolean tiny = value.signum() != 0 && value.abs().compareTo(BigDecimal.ONE) < 0;
        return format(tiny ? 4 : 2, true).format(value);
    }

    /** Введённая пользователем сумма: без лишних нулей (100, а не 100,00). */
    public String amount(BigDecimal value) {
        return format(4, false).format(value);
    }

    /** Курс: 4 знака, как в данных ЦБ. */
    public String rate(BigDecimal value) {
        return format(4, true).format(value);
    }

    public String date(Instant instant) {
        return DATE.format(instant);
    }

    public String conversion(BigDecimal amount, String from, String to, BigDecimal result, Instant date) {
        return "💱 " + amount(amount) + " " + from + " = " + money(result) + " " + to
                + "\n\nКурсы ЦБ РФ на " + date(date);
    }
}