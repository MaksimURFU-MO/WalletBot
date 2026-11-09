package client;

import org.junit.jupiter.api.Test;
import model.Rate;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CbrJsonParserTest {

    public static final String SAMPLE = """
            {
              "Date": "2026-10-24T11:30:00+03:00",
              "PreviousDate": "2026-10-23T11:30:00+03:00",
              "Valute": {
                "USD": {"ID": "R01235", "NumCode": "840", "CharCode": "USD", "Nominal": 1,
                        "Name": "Доллар США", "Value": 80.5, "Previous": 80.1},
                "JPY": {"ID": "R01820", "NumCode": "392", "CharCode": "JPY", "Nominal": 100,
                        "Name": "Японских иен", "Value": 52.5, "Previous": 52.0}
              }
            }
            """;

    private final CbrJsonParser parser = new CbrJsonParser();

    @Test
    void parsesCurrencies() throws CurrencyException {
        Map<String, Rate> rates = parser.parse(SAMPLE);

        Rate usd = rates.get("USD");
        assertEquals("Доллар США", usd.name());
        assertEquals(0, new BigDecimal("80.5").compareTo(usd.value()));
        assertEquals(1, usd.nominal());
        assertEquals(Instant.parse("2026-10-24T08:30:00Z"), usd.date());
    }

    @Test
    void keepsNominalForCurrenciesQuotedPerHundred() throws CurrencyException {
        assertEquals(100, parser.parse(SAMPLE).get("JPY").nominal());
    }

    @Test
    void addsRubleWithRateOne() throws CurrencyException {
        Rate rub = parser.parse(SAMPLE).get("RUB");

        assertEquals(0, BigDecimal.ONE.compareTo(rub.value()));
        assertEquals(1, rub.nominal());
    }

    @Test
    void rejectsInvalidJson() {
        assertBadData("это не json");
    }

    @Test
    void rejectsEmptyAndNullInput() {
        assertBadData("");
        assertBadData("   ");
        assertBadData(null);
    }

    @Test
    void rejectsResponseWithoutValuteSection() {
        assertBadData("{\"Date\": \"2026-10-24T11:30:00+03:00\"}");
    }

    @Test
    void rejectsCurrencyWithMissingFields() {
        assertBadData("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Name": "Доллар США"}}}
                """);
    }

    @Test
    void rejectsNonPositiveValue() {
        assertBadData("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": 0}}}
                """);
    }

    @Test
    void returnsEveryCurrencyFromSourcePlusRuble() throws CurrencyException {
        assertEquals(Set.of("USD", "JPY", "RUB"), parser.parse(SAMPLE).keySet());
    }

    @Test
    void emptyValuteSectionGivesOnlyRuble() throws CurrencyException {
        Map<String, Rate> rates = parser.parse("{\"Date\": \"2026-10-24T11:30:00+03:00\", \"Valute\": {}}");

        assertEquals(Set.of("RUB"), rates.keySet());
    }

    @Test
    void normalizesCurrencyCodeToUpperCase() throws CurrencyException {
        Map<String, Rate> rates = parser.parse("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"usd": {"CharCode": "usd", "Nominal": 1, "Name": "Доллар США", "Value": 80.5}}}
                """);

        assertEquals("USD", rates.get("USD").code());
    }

    @Test
    void keepsDecimalPrecisionOfValue() throws CurrencyException {
        Map<String, Rate> rates = parser.parse("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": 80.1234}}}
                """);

        assertEquals(0, new BigDecimal("80.1234").compareTo(rates.get("USD").value()));
    }

    @Test
    void everyCurrencyIncludingRubleGetsTheDocumentDate() throws CurrencyException {
        Instant expected = Instant.parse("2026-10-24T08:30:00Z");

        for (Rate rate : parser.parse(SAMPLE).values()) {
            assertEquals(expected, rate.date(), rate.code());
        }
    }

    @Test
    void rejectsMissingDate() {
        assertBadData("""
                {"Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": 80}}}
                """);
    }

    @Test
    void rejectsMalformedDate() {
        assertBadData("""
                {"Date": "вчера",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": 80}}}
                """);
    }

    @Test
    void rejectsZeroNominal() {
        assertBadData("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 0, "Name": "X", "Value": 80}}}
                """);
    }

    @Test
    void rejectsNegativeValue() {
        assertBadData("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": -5}}}
                """);
    }

    @Test
    void rejectsValueWithCommaAsDecimalSeparator() {
        assertBadData("""
                {"Date": "2026-10-24T11:30:00+03:00",
                 "Valute": {"USD": {"CharCode": "USD", "Nominal": 1, "Name": "X", "Value": "80,5"}}}
                """);
    }

    @Test
    void rejectsJsonArrayInsteadOfObject() {
        assertBadData("[]");
    }

    @Test
    void rejectsValuteThatIsNotAnObject() {
        assertBadData("{\"Date\": \"2026-10-24T11:30:00+03:00\", \"Valute\": 5}");
    }

    @Test
    void keepsOriginalErrorAsCause() {
        CurrencyException e = assertThrows(CurrencyException.class, () -> parser.parse("это не json {"));

        assertNotNull(e.getCause());
    }

    private void assertBadData(String raw) {
        CurrencyException e = assertThrows(CurrencyException.class, () -> parser.parse(raw));
        assertEquals(Reason.BAD_DATA, e.getReason());
    }
}
