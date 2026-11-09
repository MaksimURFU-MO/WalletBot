package service;

import client.RateSource;
import model.Rate;
import service.CurrencyException.Reason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

public class CbrCurrencyService implements CurrencyService {

    private static final int SCALE = 4;

    private final RateSource source;

    public CbrCurrencyService(RateSource source) {
        this.source = source;
    }

    @Override
    public Rate getRate(String currencyCode) throws CurrencyException {
        return findRate(loadRates(), currencyCode);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCode, String toCode) throws CurrencyException {
        if (amount == null || amount.signum() < 0) {
            throw new CurrencyException(Reason.INVALID_AMOUNT, "Сумма должна быть неотрицательной");
        }
        Map<String, Rate> rates = loadRates();
        Rate from = findRate(rates, fromCode);
        Rate to = findRate(rates, toCode);
        return calculate(amount, from, to);
    }

    private BigDecimal calculate(BigDecimal amount, Rate from, Rate to) {
        BigDecimal numerator = amount
                .multiply(from.value())
                .multiply(BigDecimal.valueOf(to.nominal()));
        BigDecimal denominator = to.value()
                .multiply(BigDecimal.valueOf(from.nominal()));
        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    private Map<String, Rate> loadRates() throws CurrencyException {
        return source.fetch();
    }

    private Rate findRate(Map<String, Rate> rates, String code) throws CurrencyException {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        Rate rate = rates.get(normalized);
        if (rate == null) {
            throw new CurrencyException(Reason.UNKNOWN_CURRENCY, "Неизвестная валюта: " + code);
        }
        return rate;
    }
}