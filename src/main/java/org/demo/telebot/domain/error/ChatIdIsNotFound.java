package org.demo.telebot.domain.error;

public class ChatIdIsNotFound extends RuntimeException {
    public ChatIdIsNotFound() {
    }

    public ChatIdIsNotFound(String message) {
        super(message);
    }

    public ChatIdIsNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatIdIsNotFound(Throwable cause) {
        super(cause);
    }

    public ChatIdIsNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
