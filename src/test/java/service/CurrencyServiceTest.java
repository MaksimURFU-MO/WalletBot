package service;

import org.junit.jupiter.api.Test;
import service.CurrencyException.Reason;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyExceptionTest {

    @Test
    void keepsReasonAndMessage() {
        CurrencyException e = new CurrencyException(Reason.UNKNOWN_CURRENCY, "Неизвестная валюта: XXX");

        assertEquals(Reason.UNKNOWN_CURRENCY, e.getReason());
        assertEquals("Неизвестная валюта: XXX", e.getMessage());
    }

    @Test
    void withoutCauseHasNoCause() {
        CurrencyException e = new CurrencyException(Reason.BAD_DATA, "мусор");

        assertNull(e.getCause());
    }

    @Test
    void keepsCause() {
        IOException cause = new IOException("сеть упала");

        CurrencyException e = new CurrencyException(Reason.SOURCE_UNAVAILABLE, "ЦБ недоступен", cause);

        assertSame(cause, e.getCause());
        assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
        assertEquals("ЦБ недоступен", e.getMessage());
    }




}
