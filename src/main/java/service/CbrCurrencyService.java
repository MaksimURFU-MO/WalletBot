package service;

import client.RateSource;
import model.Currency;
import model.Rate;
import service.CurrencyException.Reason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

public class CbrCurrencyService implements CurrencyService {

    private static final int SCALE = 4;

    private final RateSource source;

    public CbrCurrencyService(RateSource source) {
        this.source = source;
    }

    @Override
    public Rate getRate(Currency currency, LocalDate date) throws CurrencyException {
        LocalDate targetDate = (date == null) ? LocalDate.now() : date;
        return findRate(loadRates(targetDate), currency);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency) throws CurrencyException {
        if (amount == null || amount.signum() < 0) {
            throw new CurrencyException(Reason.INVALID_AMOUNT, "Сумма должна быть неотрицательной");
        }
        Map<Currency, Rate> rates = loadRates(LocalDate.now());
        Rate from = findRate(rates, fromCurrency);
        Rate to = findRate(rates, toCurrency);
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

    private Map<Currency, Rate> loadRates(LocalDate date) throws CurrencyException {
        return source.fetch(date);
    }

    private Rate findRate(Map<Currency, Rate> rates, Currency currency) throws CurrencyException {
        if (currency == null) {
            throw new CurrencyException(Reason.UNKNOWN_CURRENCY, "Валюта не указана");
        }
        Rate rate = rates.get(currency);
        if (rate == null) {
            throw new CurrencyException(Reason.UNKNOWN_CURRENCY, "Неизвестная валюта: " + currency.name());
        }
        return rate;
    }
}