package client;

import model.Rate;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.io.IOException;
import java.util.Map;

public class ParsedRateSource implements RateSource {

    private final RawDataSource raw;
    private final RatesParser parser;

    public ParsedRateSource(RawDataSource raw, RatesParser parser) {
        this.raw = raw;
        this.parser = parser;
    }

    @Override
    public Map<String, Rate> fetch() throws CurrencyException {
        try {
            return parser.parse(raw.fetch());
        } catch (IOException e) {
            throw new CurrencyException(Reason.SOURCE_UNAVAILABLE, "Источник курсов недоступен", e);
        }
    }
}