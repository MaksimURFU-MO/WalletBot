package client;

import model.Currency;
import model.Rate;
import service.CurrencyException;

import java.time.LocalDate;
import java.util.Map;

public interface RateSource {

    default Map<Currency, Rate> fetch() throws CurrencyException {
        return fetch(LocalDate.now());
    }

    Map<Currency, Rate> fetch(LocalDate date) throws CurrencyException;
}