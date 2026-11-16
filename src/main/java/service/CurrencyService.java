package service;

import model.Currency;
import model.Rate;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CurrencyService {

    default Rate getRate(Currency currencyCode) throws CurrencyException {
        return getRate(currencyCode, LocalDate.now());
    }

    Rate getRate(Currency currencyCode, LocalDate date) throws CurrencyException;

    BigDecimal convert(BigDecimal amount, Currency fromCode, Currency toCode) throws CurrencyException;
}