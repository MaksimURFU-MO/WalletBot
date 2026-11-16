package service;

import model.Currency;
import model.Rate;
import java.math.BigDecimal;

public interface CurrencyService {
    Rate getRate(Currency currencyCode) throws CurrencyException;
    BigDecimal convert(BigDecimal amount, Currency fromCode, Currency toCode) throws CurrencyException;
}