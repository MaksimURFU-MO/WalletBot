package model;

import java.util.Locale;
import java.util.Optional;

public enum Currency {
    RUB, AUD, AZN, GBP, AMD, BYN, BGN, BRL, HUF, VND,
    HKD, GEL, DKK, AED, USD, EUR, EGP, INR, IDR, KZT,
    CAD, QAR, KGS, CNY, MDL, NZD, NOK, PLN, RON, XDR,
    SGD, TJS, THB, TRY, TMT, UZS, UAH, CZK, SEK, CHF,
    RSD, ZAR, KRW, JPY;

    private static final Locale RU_LOCALE = Locale.forLanguageTag("ru");

    public String getTitle() {
        return java.util.Currency.getInstance(this.name()).getDisplayName(RU_LOCALE);
    }

    public static Optional<Currency> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Currency.valueOf(code.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}