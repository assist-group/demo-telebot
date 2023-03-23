package org.demo.telebot.api.output;

public class CallbackPaymentResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CallbackPaymentResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
