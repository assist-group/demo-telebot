package org.demo.telebot.api.error;

public class CallbackServerException extends RuntimeException {
    public CallbackServerException() {
    }

    public CallbackServerException(String message) {
        super(message);
    }

    public CallbackServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallbackServerException(Throwable cause) {
        super(cause);
    }

    public CallbackServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
