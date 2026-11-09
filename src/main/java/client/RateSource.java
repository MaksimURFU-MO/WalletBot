package client;

import model.Rate;
import service.CurrencyException;

import java.util.Map;

@FunctionalInterface
public interface RateSource {
    Map<String, Rate> fetch() throws CurrencyException;
}