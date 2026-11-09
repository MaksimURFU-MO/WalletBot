package service;

public class CurrencyException extends Exception {

    public enum Reason {
        UNKNOWN_CURRENCY,
        SOURCE_UNAVAILABLE,
        BAD_DATA,
        INVALID_AMOUNT
    }

    private final Reason reason;

    public CurrencyException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public CurrencyException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}