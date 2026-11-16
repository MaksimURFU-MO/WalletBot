package client;

import model.Rate;
import model.Currency;
import service.CurrencyException;

import java.util.Map;
@FunctionalInterface
public interface RateSource {
    Map<Currency, Rate> fetch() throws CurrencyException;
}