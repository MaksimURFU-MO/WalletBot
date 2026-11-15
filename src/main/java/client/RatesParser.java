package client;

import model.Rate;
import model.Currency;
import service.CurrencyException;

import java.util.Map;

public interface RatesParser {
    Map<Currency, Rate> parse(String raw) throws CurrencyException;
}