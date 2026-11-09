package client;

import model.Rate;
import service.CurrencyException;

import java.util.Map;

public interface RatesParser {
    Map<String, Rate> parse(String raw) throws CurrencyException;
}