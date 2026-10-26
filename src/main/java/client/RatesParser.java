package client;

import model.Rate;
import service.CurrencyException;

import java.util.Map;

@FunctionalInterface
public interface RatesParser {
    Map<String, Rate> parse(String raw) throws CurrencyException;
}