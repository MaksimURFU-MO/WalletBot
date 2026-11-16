package model;

import java.math.BigDecimal;
import java.time.Instant;

public record Rate(String code, String name, BigDecimal value, int nominal, Instant date) {

    public static final String RUB = "RUB";

    public Rate {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Код валюты не может быть пустым");
        }
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Курс должен быть положительным: " + code);
        }
        if (nominal <= 0) {
            throw new IllegalArgumentException("Номинал должен быть положительным: " + code);
        }
    }

    public static Rate rub(Instant date) {
        return new Rate(RUB, "Российский рубль", BigDecimal.ONE, 1, date);
    }
}