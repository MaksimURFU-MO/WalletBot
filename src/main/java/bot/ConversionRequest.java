package bot;

import model.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public record ConversionRequest(BigDecimal amount, Currency from, Currency to) {

    private static final Set<String> SEPARATORS = Set.of("->", "=>", "→", "в", "to", "in");

    public static Optional<ConversionRequest> parse(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        List<String> tokens = List.of(text.trim().split("\\s+")).stream()
                .filter(token -> !SEPARATORS.contains(token.toLowerCase(Locale.ROOT)))
                .toList();

        if (tokens.size() != 3) {
            return Optional.empty();
        }

        Optional<BigDecimal> amount = parseAmount(tokens.get(0));
        Optional<Currency> from = Currency.fromCode(tokens.get(1));
        Optional<Currency> to = Currency.fromCode(tokens.get(2));

        if (amount.isEmpty() || from.isEmpty() || to.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ConversionRequest(amount.get(), from.get(), to.get()));
    }

    private static Optional<BigDecimal> parseAmount(String raw) {
        try {
            BigDecimal value = new BigDecimal(raw.replace(',', '.'));
            return value.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}