package service;

import model.Rate;
import java.math.BigDecimal;

public interface CurrencyService {
    Rate getRate(String currencyCode) throws CurrencyException;
    BigDecimal convert(BigDecimal amount, String fromCode, String toCode) throws CurrencyException;
}