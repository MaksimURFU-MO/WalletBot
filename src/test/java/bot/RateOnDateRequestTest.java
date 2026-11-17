package bot;

import model.Currency;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RateOnDateRequestTest {

    private static RateOnDateRequest ok(String text) {
        return RateOnDateRequest.parse(text).orElseThrow(() -> new AssertionError("не разобралось: " + text));
    }

    private static void assertRejected(String text) {
        assertTrue(RateOnDateRequest.parse(text).isEmpty(), "должно быть отклонено: " + text);
    }

    // --- форматы даты ---

    @Test
    void parsesDotDate() {
        RateOnDateRequest r = ok("курс usd 14.11.2026");
        assertEquals(Currency.USD, r.currency());
        assertEquals(LocalDate.of(2026, 11, 14), r.date());
    }

    @Test
    void parsesDotDateWithoutLeadingZeros() {
        assertEquals(LocalDate.of(2026, 2, 1), ok("курс usd 1.2.2026").date());
    }

    @Test
    void parsesIsoDate() {
        assertEquals(LocalDate.of(2026, 11, 14), ok("курс usd 2026-11-14").date());
    }

    @Test
    void parsesDateWithWords() {
        RateOnDateRequest r = ok("курс евро на 1 января 2026");
        assertEquals(Currency.EUR, r.currency());
        assertEquals(LocalDate.of(2026, 1, 1), r.date());
    }

    @Test
    void parsesNominativeMonthName() {
        assertEquals(LocalDate.of(2026, 1, 1), ok("курс usd 1 январь 2026").date());
    }

    @Test
    void parsesAllMonthsInGenitive() {
        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        for (int i = 0; i < months.length; i++) {
            assertEquals(LocalDate.of(2026, i + 1, 10), ok("курс usd 10 " + months[i] + " 2026").date(), months[i]);
        }
    }

    @Test
    void ignoresTrailingWordAfterYear() {
        assertEquals(LocalDate.of(2026, 1, 1), ok("курс usd 1 января 2026 года").date());
        assertEquals(LocalDate.of(2026, 1, 1), ok("курс usd 1 января 2026г.").date());
    }

    // --- слово «на» и регистр ---

    @Test
    void wordNaIsOptional() {
        assertEquals(ok("курс usd 14.11.2026"), ok("курс usd на 14.11.2026"));
        assertEquals(ok("курс евро 1 января 2026"), ok("курс евро на 1 января 2026"));
    }

    @Test
    void isCaseInsensitiveAndToleratesSpaces() {
        RateOnDateRequest r = ok("  Курс  USD   на   14.11.2026  ");
        assertEquals(Currency.USD, r.currency());
        assertEquals(LocalDate.of(2026, 11, 14), r.date());
    }

    // --- валюта ---

    @Test
    void parsesCurrencyByCodeAndByRussianName() {
        assertEquals(Currency.USD, ok("курс доллар 14.11.2026").currency());
        assertEquals(Currency.USD, ok("курс доллара 14.11.2026").currency());
        assertEquals(Currency.EUR, ok("курс евро 14.11.2026").currency());
        assertEquals(Currency.CNY, ok("курс юань 14.11.2026").currency());
        assertEquals(Currency.JPY, ok("курс иены 14.11.2026").currency());
        assertEquals(Currency.KZT, ok("курс тенге 14.11.2026").currency());
        assertEquals(Currency.RUB, ok("курс рубль 14.11.2026").currency());
    }

    @Test
    void rejectsUnknownCurrency() {
        assertRejected("курс бла 14.11.2026");
    }

    // --- отказы ---

    @Test
    void rejectsNullAndBlank() {
        assertEquals(Optional.empty(), RateOnDateRequest.parse(null));
        assertEquals(Optional.empty(), RateOnDateRequest.parse(""));
        assertEquals(Optional.empty(), RateOnDateRequest.parse("   "));
    }

    @Test
    void rejectsTextWithoutWordKurs() {
        assertRejected("usd 14.11.2026");
        assertRejected("100 USD RUB");
        assertRejected("USD");
    }

    @Test
    void rejectsKursWithoutDate() {
        assertRejected("курс usd");
        assertRejected("курс usd на");
    }

    @Test
    void rejectsUnparsableDate() {
        assertRejected("курс usd вчера");
        assertRejected("курс usd 14/11/2026");
        assertRejected("курс usd 1 января");          // нет года
        assertRejected("курс usd 1 января 26");       // год не из 4 цифр
        assertRejected("курс usd 1 январии 2026");    // неизвестный месяц
    }

    @Test
    void rejectsNonexistentDayInWordsFormat() {
        assertRejected("курс usd 31 февраля 2026");
        assertRejected("курс usd 0 января 2026");
    }
}