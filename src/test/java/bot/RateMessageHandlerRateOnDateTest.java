package bot;

import model.Currency;
import model.Rate;
import org.junit.jupiter.api.Test;
import service.CurrencyException;
import service.CurrencyService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RateMessageHandlerRateOnDateTest {

    private static class FakeService implements CurrencyService {
        Rate rateToReturn;
        CurrencyException toThrow;
        Currency lastCurrency;
        LocalDate lastDate;
        int getRateCalls;

        @Override
        public Rate getRate(Currency currency, LocalDate date) throws CurrencyException {
            getRateCalls++;
            lastCurrency = currency;
            lastDate = date;
            if (toThrow != null) {
                throw toThrow;
            }
            return rateToReturn;
        }

        @Override
        public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
            throw new UnsupportedOperationException("convert не должен вызываться");
        }
    }

    private static Rate usd(String isoInstant) {
        return new Rate("USD", "Доллар США", new BigDecimal("80.5"), 1, Instant.parse(isoInstant));
    }

    private final FakeService service = new FakeService();
    private final RateMessageHandler handler = new RateMessageHandler(service);

    @Test
    void passesParsedCurrencyAndDateToService() {
        service.rateToReturn = usd("2026-11-14T08:30:00Z");

        handler.handle("курс usd 14.11.2026");

        assertEquals(Currency.USD, service.lastCurrency);
        assertEquals(LocalDate.of(2026, 11, 14), service.lastDate);
        assertEquals(1, service.getRateCalls);
    }

    @Test
    void answerContainsRateAndRequestedDate() {
        service.rateToReturn = usd("2026-11-14T08:30:00Z");

        String answer = handler.handle("курс доллара на 14.11.2026");

        assertTrue(answer.contains("Курс USD (Доллар США) на 14.11.2026"), answer);
        assertTrue(answer.contains("80.5 руб. за 1 ед."), answer);
    }

    @Test
    void answerShowsNominal() {
        service.rateToReturn = new Rate("JPY", "Японских иен", new BigDecimal("52.1"), 100,
                Instant.parse("2026-11-14T08:30:00Z"));

        String answer = handler.handle("курс jpy 14.11.2026");

        assertTrue(answer.contains("52.1 руб. за 100 ед."), answer);
    }

    @Test
    void answerMentionsActualCbrDate() {
        service.rateToReturn = usd("2026-11-14T08:30:00Z");
        assertTrue(handler.handle("курс usd 14.11.2026").contains("фактическая дата ЦБ"));
    }

    /**
     * Сейчас обработчик печатает rate.date() как Instant (2026-11-13T08:30:00Z).
     * Тест описывает ожидаемое поведение и упадёт, пока там не будет formatter.date(rate.date()).
     */
    @Test
    void actualCbrDateIsShownAsDdMmYyyy() {
        // запросили субботу, ЦБ отдал пятницу
        service.rateToReturn = usd("2026-11-13T08:30:00Z");

        String answer = handler.handle("курс usd 14.11.2026");

        assertTrue(answer.contains("фактическая дата ЦБ: 13.11.2026"), answer);
    }

    @Test
    void futureDateIsRejectedWithoutCallingService() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        String answer = handler.handle("курс usd " + tomorrow);

        assertEquals("❌ Нельзя узнать курс на будущую дату.", answer);
        assertEquals(0, service.getRateCalls);
    }

    @Test
    void todayIsAllowed() {
        service.rateToReturn = usd("2026-11-14T08:30:00Z");

        String answer = handler.handle("курс usd " + LocalDate.now());

        assertFalse(answer.startsWith("❌"), answer);
        assertEquals(1, service.getRateCalls);
    }

    @Test
    void serviceErrorIsShownToUser() {
        service.toThrow = new CurrencyException(CurrencyException.Reason.SOURCE_UNAVAILABLE,
                "Архив курсов на дату 2026-03-08 не найден");

        assertEquals("❌ Ошибка: Архив курсов на дату 2026-03-08 не найден",
                handler.handle("курс usd 08.03.2026"));
    }

    @Test
    void unknownCurrencyErrorIsShownToUser() {
        service.toThrow = new CurrencyException(CurrencyException.Reason.UNKNOWN_CURRENCY, "Неизвестная валюта: USD");

        assertEquals("❌ Ошибка: Неизвестная валюта: USD", handler.handle("курс usd 14.11.2026"));
    }

    @Test
    void dateRequestHasPriorityOverOtherCommands() {
        service.rateToReturn = usd("2026-11-14T08:30:00Z");

        // convert() у FakeService бросает UnsupportedOperationException — если бы вызвали, тест упал бы
        assertDoesNotThrow(() -> handler.handle("курс usd 14.11.2026"));
        assertEquals(LocalDate.of(2026, 11, 14), service.lastDate);
    }

    @Test
    void textWithoutDateIsNotTreatedAsDateRequest() {
        String answer = handler.handle("курс usd");

        assertEquals(0, service.getRateCalls);
        assertTrue(answer.startsWith("Не понял команду"), answer);
    }
}